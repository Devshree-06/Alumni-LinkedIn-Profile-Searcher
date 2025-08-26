package com.Alumni_LinkedIn_Profile_Searcher.service;

import com.Alumni_LinkedIn_Profile_Searcher.model.Request.ALumniSearchReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PhantomIntegrationServiceTest {

    @InjectMocks
    private PhantomIntegrationService phantomIntegrationService;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.Builder webClientBuilder;


    @BeforeEach
    void setUp() {
        when(webClientBuilder.baseUrl("https://api.phantombuster.com/api/v2")).thenReturn(webClientBuilder);
        when(webClientBuilder.codecs(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        phantomIntegrationService = new PhantomIntegrationService(webClientBuilder);
    }

    @Test
    void testGetLinkedInSearch_success() {
        ALumniSearchReq request = new ALumniSearchReq();
        request.setUniversity("VIT");
        request.setDesignation("Software Engineer");
        request.setPassoutYear("2023");

        Flux<Map<String, Object>> mockFlux = Flux.just(Map.of("fullName", "John Doe"));

        StepVerifier.create(mockFlux)
                .expectNextMatches(map -> "John Doe".equals(map.get("fullName")))
                .verifyComplete();
    }
}
