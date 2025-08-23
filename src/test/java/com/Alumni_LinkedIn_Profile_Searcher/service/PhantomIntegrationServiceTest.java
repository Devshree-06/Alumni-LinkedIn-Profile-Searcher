package com.Alumni_LinkedIn_Profile_Searcher.service;

import com.Alumni_LinkedIn_Profile_Searcher.model.Request.ALumniSearchReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import java.util.Map;

import static org.mockito.Mockito.when;




public class PhantomIntegrationServiceTest {

    @InjectMocks
    private PhantomIntegrationService phantomIntegrationService;

    @Mock
    private WebClient webClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetLinkedInSearch_success() {
        ALumniSearchReq request = new ALumniSearchReq();
        request.setUniversity("vellore institute of technology (vit)");
        request.setDesignation("Software Engineer");
        request.setPassoutYear("2023");

        when(phantomIntegrationService.getLinkedInSearch(request))
                .thenReturn(Flux.just(Map.of(
                        "fullName", "John Doe",
                        "jobTitle", "Software Engineer",
                        "school", "VIT",
                        "location", "India",
                        "headline", "Software Developer"
                )));

        StepVerifier.create(phantomIntegrationService.getLinkedInSearch(request))
                .expectNextMatches(map -> "John Doe".equals(map.get("fullName")))
                .verifyComplete();
    }
}
