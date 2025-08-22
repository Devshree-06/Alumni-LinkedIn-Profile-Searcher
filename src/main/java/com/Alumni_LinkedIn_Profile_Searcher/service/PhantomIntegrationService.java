package com.Alumni_LinkedIn_Profile_Searcher.service;

import com.Alumni_LinkedIn_Profile_Searcher.model.Request.ALumniSearchReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class PhantomIntegrationService {

    @Autowired
    public WebClient phantomWebClient;

    @Value("${Phantom.apiKey}")
    private String apiKey;
    @Value("${Phantom.baseUrl}")
    private String baseUrl;
    @Value("${Phantom.linkedInUrl}")
    private String linkedInUrl;
    @Value("${Phantom.agentId}")
    private String agentId;
    @Value("${Phantom.sessionCookie}")
    private String sessionCookie;


    public Flux<Map<String,Object>> getLinkedInSearch(ALumniSearchReq request){

        String query = request.getUniversity() + " " + request.getDesignation() + " " +
                request.getPassoutYear();

        String linkedInSearchUrl = linkedInUrl + query.replace(" ","%20");

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("search", query);
        arguments.put("sessionCookie", sessionCookie);
        arguments.put("numberOfResults", 10);

        return phantomWebClient.post()
                .uri(baseUrl+"/agents/launch")
                .header("X-Phantombuster-Key-1", apiKey)
                .bodyValue(Map.of(
                        "id", agentId,
                        "argument",arguments
                ))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String,Object>>() {})
                .flatMapMany(launchResponse -> {
                    String containerId = launchResponse.get("containerId").toString();

                    return Flux.interval(Duration.ofSeconds(5))
                            .flatMap(tick ->
                                    phantomWebClient.get()
                                            .uri(baseUrl + "/containers/fetch-output?id=" + containerId)
                                            .header("X-Phantombuster-Key-1", apiKey)
                                            .retrieve()
                                            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                            )
                            .filter(resp -> {
                                Object isRunning = resp.get("isAgentRunning");
                                return isRunning != null && !(Boolean) isRunning;
                            })
                            .next()
                            .timeout(Duration.ofMinutes(2))
                            .onErrorResume(TimeoutException.class,
                                    e -> Mono.error(new RuntimeException("Phantom scraping timed out")))
                            .flatMapMany(resp -> {
                                Object results = resp.get("resultObject");
                                if (results instanceof List) {
                                    return Flux.fromIterable((List<Map<String, Object>>) results);
                                } else {
                                    return Flux.error(new RuntimeException("No resultObject found, check CSV export"));
                                }
                            });
                });
    }

}
