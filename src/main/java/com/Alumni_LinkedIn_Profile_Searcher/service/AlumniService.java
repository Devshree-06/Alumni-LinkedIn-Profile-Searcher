package com.Alumni_LinkedIn_Profile_Searcher.service;

import lombok.extern.slf4j.Slf4j;
import com.Alumni_LinkedIn_Profile_Searcher.model.Alumni;
import com.Alumni_LinkedIn_Profile_Searcher.model.Request.ALumniSearchReq;
import com.Alumni_LinkedIn_Profile_Searcher.model.Response.AlumniSearchRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.Alumni_LinkedIn_Profile_Searcher.repository.AlumniRepository;


@Service
@Slf4j
public class AlumniService {

    @Autowired
    AlumniRepository alumniRepository;

    @Autowired
    PhantomIntegrationService phantomIntegrationService;

    public Mono<ResponseEntity<Alumni>> saveAlumni(Alumni alumni) {
        return alumniRepository.save(alumni)
                .map(savedAlumni-> ResponseEntity.ok(savedAlumni));
    }

    // Fetch all alumni from DB
    public Flux<ResponseEntity<Alumni>> getAllAlumni() {
        return alumniRepository.findAll()
                .map(alumni->ResponseEntity.ok(alumni));
    }

    public Mono<ResponseEntity<AlumniSearchRes>> searchAlumni(ALumniSearchReq request){

        return phantomIntegrationService.getLinkedInSearch(request)
                .collectList()
                .map(result->{
                    log.info("The result of the Phantom API is : {}", result);

                    return ResponseEntity.ok(
                            AlumniSearchRes.builder()
                                    .status("success")
                                    .message("Raw Phantom API response")
                                    .data(result) // <-- directly putting raw list here
                                    .build()
                    );
                });

    }

}
