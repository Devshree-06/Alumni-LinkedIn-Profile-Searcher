package com.Alumni_LinkedIn_Profile_Searcher.service;

import com.Alumni_LinkedIn_Profile_Searcher.exception.PhantomAPIException;
import com.Alumni_LinkedIn_Profile_Searcher.exception.PhantomTimeOutException;
import com.Alumni_LinkedIn_Profile_Searcher.model.Request.ALumniSearchReq;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${Phantom.apiKey}")
    private String apiKey;
    @Value("${Phantom.baseUrl}")
    private String baseUrl;
    @Value("${Phantom.agentId}")
    private String agentId;
    @Value("${Phantom.sessionCookie}")
    private String sessionCookie;

    @Autowired
    public PhantomIntegrationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }


    public Flux<Map<String, Object>> getLinkedInSearch(ALumniSearchReq request) {
        String query = buildQuery(request);

        Map<String, Object> arguments = Map.of(
                "search", query,
                "sessionCookie", sessionCookie,
                "numberOfResults", 10
        );

        return launchAgent(arguments)
                .flatMapMany(containerId ->
                        pollContainerUntilFinished(containerId, 120, Duration.ofSeconds(5))
                                .flatMapMany(this::fetchOutputAndParse)
                );
    }

    private String buildQuery(ALumniSearchReq request) {
        return String.format("%s %s %s",
                Optional.ofNullable(request.getUniversity()).orElse(""),
                Optional.ofNullable(request.getDesignation()).orElse(""),
                Optional.ofNullable(request.getPassoutYear()).orElse("")
        ).trim();
    }

    private Mono<String> launchAgent(Map<String, Object> arguments) {
        return webClient.post()
                .uri(baseUrl + "/agents/launch")
                .header("X-Phantombuster-Key-1", apiKey)
                .bodyValue(Map.of("id", agentId, "argument", arguments))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(resp -> {
                    String containerId = Objects.toString(resp.get("containerId"), null);
                    if (containerId == null) {
                        throw new PhantomAPIException("No containerId found in launch response");
                    }
                    log.info("Phantom launched with container ID: {}", containerId);
                    return containerId;
                });
    }

    private Mono<Map<String, Object>> pollContainerUntilFinished(String containerId, int attemptsLeft, Duration delay) {
        return webClient.get()
                .uri(baseUrl + "/containers/fetch?id=" + containerId)
                .header("X-Phantombuster-Key-1", apiKey)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .flatMap(resp -> {
                    String status = (String) resp.get("status");
                    log.info("Container {} status: {}", containerId, status);

                    if (status == null) {
                        return Mono.error(new PhantomAPIException("No status in container fetch response"));
                    }
                    if ("failed".equalsIgnoreCase(status)) {
                        return Mono.error(new PhantomAPIException("Phantom scraping failed"));
                    }
                    if ("finished".equalsIgnoreCase(status)) {
                        return Mono.just(resp);
                    }
                    if (attemptsLeft <= 0) {
                        return Mono.error(new PhantomTimeOutException("Phantom scraping timed out"));
                    }

                    return Mono.delay(delay)
                            .then(pollContainerUntilFinished(containerId, attemptsLeft - 1, delay));
                });
    }

    private Flux<Map<String, Object>> fetchOutputAndParse(Map<String, Object> fetchResponse) {
        String containerId = Objects.toString(fetchResponse.get("id"), null);
        if (containerId == null) {
            return Flux.error(new PhantomAPIException("No containerId in fetch response"));
        }

        return webClient.get()
                .uri(baseUrl + "/containers/fetch-output?id=" + containerId)
                .header("X-Phantombuster-Key-1", apiKey)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .flatMapMany(resp -> {
                    String logs = (String) resp.get("output");
                    if (logs == null || logs.isEmpty()) {
                        return Flux.error(new PhantomAPIException("No output logs found"));
                    }

                    return extractDataFromLogs(logs);
                });
    }

    private Flux<Map<String, Object>> extractDataFromLogs(String logs) {
        String jsonUrl = extractUrl(logs, "json");
        String csvUrl = extractUrl(logs, "csv");

        if (jsonUrl != null) {
            log.info("JSON URL extracted: {}", jsonUrl);
            return fetchAndParseJson(jsonUrl);
        }
        if (csvUrl != null) {
            log.info("CSV URL extracted: {}", csvUrl);
            return fetchAndParseCsv(csvUrl);
        }

        return Flux.error(new PhantomAPIException("No CSV or JSON URL found in container logs"));
    }

    private String extractUrl(String logs, String extension) {
        Matcher matcher = Pattern.compile("https://phantombuster\\.s3\\.amazonaws\\.com/\\S+\\." + extension)
                .matcher(logs);
        return matcher.find() ? matcher.group() : null;
    }

    private Flux<Map<String, Object>> fetchAndParseJson(String url) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .flatMapMany(fileContent -> {
                    try {
                        List<Map<String, Object>> list = objectMapper.readValue(fileContent, List.class);
                        return Flux.fromIterable(list);
                    } catch (Exception e) {
                        return Flux.error(new PhantomAPIException("Failed to parse JSON file", e));
                    }
                });
    }

    private Flux<Map<String, Object>> fetchAndParseCsv(String url) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .flatMapMany(fileContent -> {
                    try {
                        CSVParser parser = CSVFormat.DEFAULT
                                .withFirstRecordAsHeader()
                                .parse(new StringReader(fileContent));

                        List<Map<String, Object>> list = new ArrayList<>();
                        for (CSVRecord rec : parser) {
                            list.add(new HashMap<>(rec.toMap()));
                        }
                        return Flux.fromIterable(list);
                    } catch (Exception e) {
                        return Flux.error(new PhantomAPIException("Failed to parse CSV file", e));
                    }
                });
    }
}
