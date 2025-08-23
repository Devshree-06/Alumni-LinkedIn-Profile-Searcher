package com.Alumni_LinkedIn_Profile_Searcher.service;

import com.Alumni_LinkedIn_Profile_Searcher.DTO.AlumniSearchDTO;
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

import java.util.ArrayList;
import java.util.List;


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

    public Flux<Alumni> getAllAlumni() {
        return alumniRepository.findAll()
                .doOnNext(a -> log.info("Alumni fetched: {}", a.getName()));
    }

    public Mono<ResponseEntity<AlumniSearchRes>> searchAlumni(ALumniSearchReq request){

        return phantomIntegrationService.getLinkedInSearch(request)
                .map(result->{

                    AlumniSearchDTO alumniFields = new AlumniSearchDTO();
                    alumniFields.setName((String) result.getOrDefault("fullName",""));
                    alumniFields.setCurrentRole((String) result.getOrDefault("jobTitle",""));
                    alumniFields.setUniversity((String) result.getOrDefault("school",""));
                    alumniFields.setLocation((String) result.getOrDefault("location",""));
                    alumniFields.setLinkedInHeadline((String) result.getOrDefault("headline",""));

                    String passOutYear = (String) result.get("schoolDateRange");
                    if(passOutYear!=null && passOutYear.contains("-")){
                        String [] parts = passOutYear.split("-");
                        alumniFields.setPassoutYear(parts[1].trim());
                    }

                    return alumniFields;

                })
                .collectList()
                .flatMap(alumniDTOList -> {
                    List<Alumni> alumniEntities = new ArrayList<>();
                    for (AlumniSearchDTO dto : alumniDTOList) {
                        Alumni entity = new Alumni();
                        entity.setName(dto.getName());
                        entity.setCurrentRole(dto.getCurrentRole());
                        entity.setUniversity(dto.getUniversity());
                        entity.setLocation(dto.getLocation());
                        entity.setLinkedinHeadline(dto.getLinkedInHeadline());
                        entity.setPassoutYear(dto.getPassoutYear());
                        alumniEntities.add(entity);
                    }

                    return alumniRepository.saveAll(alumniEntities)
                            .collectList()
                            .map(savedEntities -> {
                                log.info("Saved alumni data: {}", savedEntities);

                                return ResponseEntity.ok(
                                        AlumniSearchRes.builder()
                                                .status("Success")
                                                .message("LinkedIn data fetched and saved successfully.")
                                                .data(alumniDTOList)
                                                .build()
                                );
                            });
                });

    }

}
