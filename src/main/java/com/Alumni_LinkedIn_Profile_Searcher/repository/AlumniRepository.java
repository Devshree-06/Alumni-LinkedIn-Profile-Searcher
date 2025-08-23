package com.Alumni_LinkedIn_Profile_Searcher.repository;

import com.Alumni_LinkedIn_Profile_Searcher.model.Alumni;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlumniRepository extends ReactiveCrudRepository<Alumni,Integer> {

}
