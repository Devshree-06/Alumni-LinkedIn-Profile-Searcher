package controller;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.Alumni;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import service.AlumniService;

@RestController
@RequestMapping("/api/alumni")
@Slf4j
public class AlumniController {

    @Autowired
    AlumniService alumniService;

    @PostMapping("/search")
    public Mono<ResponseEntity<Alumni>> saveAlumni(@RequestBody Alumni alumni){
        return alumniService.saveAlumni(alumni)
                .doOnNext(res-> log.info("Alumni details saved successfully"));
    }

    @GetMapping("/all")
    public Flux<ResponseEntity<Alumni>> findAllAlumni(){
        return alumniService.getAllAlumni()
                .doOnNext(res-> log.info("Alumni details fetched successfully"));
    }
}
