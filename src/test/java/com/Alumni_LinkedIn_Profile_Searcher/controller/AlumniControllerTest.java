package com.Alumni_LinkedIn_Profile_Searcher.controller;

import com.Alumni_LinkedIn_Profile_Searcher.model.Alumni;
import com.Alumni_LinkedIn_Profile_Searcher.repository.AlumniRepository;
import com.Alumni_LinkedIn_Profile_Searcher.service.AlumniService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.Mockito.*;

public class AlumniControllerTest {

    @InjectMocks
    private AlumniController alumniController;

    @Mock
    private AlumniService alumniService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webTestClient = WebTestClient.bindToController(alumniController).build();
    }

    @Test
    void testFindAllAlumni() {
        Alumni alumni1 = new Alumni(1, "John Doe", "Software Engineer", "VIT", "India", "Headline1", "2023");
        Alumni alumni2 = new Alumni(2, "Jane Smith", "Developer", "IIT", "India", "Headline2", "2022");

        when(alumniService.getAllAlumni()).thenReturn(Flux.just(alumni1, alumni2));

        webTestClient.get().uri("/all")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Alumni.class)
                .hasSize(2)
                .contains(alumni1, alumni2);

        verify(alumniService, times(1)).getAllAlumni();
    }
}

