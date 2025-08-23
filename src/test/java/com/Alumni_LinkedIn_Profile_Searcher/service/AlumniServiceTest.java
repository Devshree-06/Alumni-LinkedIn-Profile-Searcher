package com.Alumni_LinkedIn_Profile_Searcher.service;

import com.Alumni_LinkedIn_Profile_Searcher.model.Alumni;
import com.Alumni_LinkedIn_Profile_Searcher.repository.AlumniRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.*;

public class AlumniServiceTest {

    @InjectMocks
    private AlumniService alumniService;

    @Mock
    private AlumniRepository alumniRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllAlumni() {
        Alumni alumni1 = new Alumni(1, "John Doe", "Software Engineer", "VIT", "India", "Headline1", "2023");
        Alumni alumni2 = new Alumni(2, "Jane Smith", "Developer", "IIT", "India", "Headline2", "2022");

        when(alumniRepository.findAll()).thenReturn(Flux.just(alumni1, alumni2));

        Mono<List<Alumni>> result = alumniService.getAllAlumni()
                .collectList();

        StepVerifier.create(result)
                .expectNextMatches(list -> list.size() == 2
                        && list.get(0).getName().equals("John Doe")
                        && list.get(1).getName().equals("Jane Smith"))
                .verifyComplete();

        verify(alumniRepository, times(1)).findAll();
    }
}
