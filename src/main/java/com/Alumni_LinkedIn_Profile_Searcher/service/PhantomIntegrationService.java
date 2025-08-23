package com.Alumni_LinkedIn_Profile_Searcher.service;

import com.Alumni_LinkedIn_Profile_Searcher.model.Request.ALumniSearchReq;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.StringReader;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PhantomIntegrationService {

    @Autowired
    public WebClient.Builder webClientBuilder;


    private WebClient phantomWebClient() {
        return webClientBuilder
                .codecs(configurer ->
                        configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }
    @Value("${Phantom.apiKey}")
    private String apiKey;
    @Value("${Phantom.baseUrl}")
    private String baseUrl;
    @Value("${Phantom.agentId}")
    private String agentId;
    @Value("${Phantom.sessionCookie}")
    private String sessionCookie;

    public Flux<Map<String, Object>> getLinkedInSearch(ALumniSearchReq request) {

        String query = String.format("%s %s %s",
                request.getUniversity() != null ? request.getUniversity() : "",
                request.getDesignation() != null ? request.getDesignation() : "",
                request.getPassoutYear() != null ? request.getPassoutYear() : "").trim();

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("search", query);
        arguments.put("sessionCookie", sessionCookie);
        arguments.put("numberOfResults", 10);

        // Launch Phantom agent
        return phantomWebClient().post()
                .uri(baseUrl + "/agents/launch")
                .header("X-Phantombuster-Key-1", apiKey)
                .bodyValue(Map.of("id", agentId, "argument", arguments))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .flatMapMany(launchResponse -> {
                    String containerId = launchResponse.get("containerId").toString();
                    log.info("Phantom launched with container ID: {}", containerId);

                    return pollContainerStatus(containerId, 120, Duration.ofSeconds(5))
                            .flatMapMany(resp -> fetchOutputAndParse(resp));
                });
    }

    private Mono<Map<String, Object>> pollContainerStatus(String containerId, int remainingAttempts, Duration delay) {
        return Mono.defer(() ->
                phantomWebClient().get()
                        .uri(baseUrl + "/containers/fetch?id=" + containerId)
                        .header("X-Phantombuster-Key-1", apiKey)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .flatMap(resp -> {
                            log.info("Full fetch response: {}", resp);

                            String status = (String) resp.get("status");
                            if (status == null) {
                                return Mono.error(new RuntimeException("No status found in container fetch response"));
                            }

                            log.info("Container {} status: {}", containerId, status);

                            if ("failed".equalsIgnoreCase(status)) {
                                return Mono.error(new RuntimeException("Phantom scraping failed"));
                            }

                            if ("finished".equalsIgnoreCase(status)) {
                                return Mono.just(resp);
                            }

                            if (remainingAttempts <= 0) {
                                return Mono.error(new RuntimeException("Phantom scraping timed out"));
                            }

                            return Mono.delay(delay)
                                    .then(pollContainerStatus(containerId, remainingAttempts - 1, delay));
                        })
        );
    }

    private Flux<Map<String, Object>> fetchOutputAndParse(Map<String, Object> fetchResponse) {
        String containerId = fetchResponse.get("id").toString();

        return phantomWebClient().get()
                .uri(baseUrl + "/containers/fetch-output?id=" + containerId)
                .header("X-Phantombuster-Key-1", apiKey)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .flatMapMany(resp -> {
                    String logs = (String) resp.get("output");
                    if (logs == null || logs.isEmpty()) {
                        return Flux.error(new RuntimeException("No output logs found"));
                    }

                    Pattern urlPattern = Pattern.compile("https://phantombuster\\.s3\\.amazonaws\\.com/\\S+\\.(csv|json)");
                    Matcher matcher = urlPattern.matcher(logs);

                    String csvUrl = null;
                    String jsonUrl = null;
                    while (matcher.find()) {
                        String url = matcher.group();
                        if (url.endsWith(".csv")) csvUrl = url;
                        else if (url.endsWith(".json")) jsonUrl = url;
                    }

                    if (jsonUrl != null) {
                        log.info("JSON URL extracted: {}", jsonUrl);
                        return phantomWebClient().get()
                                .uri(jsonUrl)
                                .retrieve()
                                .bodyToMono(String.class)
                                .flatMapMany(fileContent -> {
                                    try {
                                        List<Map<String, Object>> list = new ArrayList<>();
                                        var jsonArray = new com.fasterxml.jackson.databind.ObjectMapper().readValue(fileContent, List.class);
                                        for (Object obj : jsonArray) {
                                            if (obj instanceof Map) list.add((Map<String, Object>) obj);
                                        }
                                        return Flux.fromIterable(list);
                                    } catch (Exception e) {
                                        return Flux.error(new RuntimeException("Failed to parse JSON file", e));
                                    }
                                });
                    } else if (csvUrl != null) {
                        log.info("CSV URL extracted: {}", csvUrl);
                        return phantomWebClient().get()
                                .uri(csvUrl)
                                .retrieve()
                                .bodyToMono(String.class)
                                .flatMapMany(fileContent -> {
                                    try {
                                        CSVParser parser = CSVFormat.DEFAULT
                                                .withFirstRecordAsHeader()
                                                .parse(new StringReader(fileContent));
                                        List<Map<String, Object>> list = new ArrayList<>();
                                        for (CSVRecord rec : parser) {
                                            Map<String, Object> row = new HashMap<>();
                                            rec.toMap().forEach(row::put);
                                            list.add(row);
                                        }
                                        return Flux.fromIterable(list);
                                    } catch (Exception e) {
                                        return Flux.error(new RuntimeException("Failed to parse CSV file", e));
                                    }
                                });
                    } else {
                        return Flux.error(new RuntimeException("No CSV or JSON URL found in container logs"));
                    }
                });
    }
}
