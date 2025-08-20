package service;

import model.Alumni;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import repository.AlumniRepository;


@Service
public class AlumniService {

    @Autowired
    AlumniRepository alumniRepository;

    public Mono<ResponseEntity<Alumni>> saveAlumni(Alumni alumni) {
        return alumniRepository.save(alumni)
                .map(savedAlumni-> ResponseEntity.ok(savedAlumni));
    }

    // Fetch all alumni from DB
    public Flux<ResponseEntity<Alumni>> getAllAlumni() {
        return alumniRepository.findAll()
                .map(alumni->ResponseEntity.ok(alumni));
    }
}
