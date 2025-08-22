package com.Alumni_LinkedIn_Profile_Searcher.controller;

import lombok.extern.slf4j.Slf4j;
import com.Alumni_LinkedIn_Profile_Searcher.model.Alumni;
import com.Alumni_LinkedIn_Profile_Searcher.model.Request.ALumniSearchReq;
import com.Alumni_LinkedIn_Profile_Searcher.model.Response.AlumniSearchRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.Alumni_LinkedIn_Profile_Searcher.service.AlumniService;

@RestController
@RequestMapping("/api/alumni")
@Slf4j
public class AlumniController {

    @Autowired
    AlumniService alumniService;

    @PostMapping("/searchAlumni")
    public Mono<ResponseEntity<Alumni>> saveAlumni(@RequestBody Alumni alumni){
        return alumniService.saveAlumni(alumni)
                .doOnNext(res-> log.info("Alumni details saved successfully"));
    }

    @GetMapping("/all")
    public Flux<ResponseEntity<Alumni>> findAllAlumni(){
        return alumniService.getAllAlumni()
                .doOnNext(res-> log.info("Alumni details fetched successfully"));
    }

    @PostMapping("/search")
    public Mono<ResponseEntity<AlumniSearchRes>> searchAlumni(@RequestBody ALumniSearchReq request){
        return alumniService.searchAlumni(request)
                .doOnNext(res-> log.info("Alumni details fetched successfully"));
    }
}
