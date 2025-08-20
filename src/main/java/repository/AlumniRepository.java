package repository;

import model.Alumni;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlumniRepository extends ReactiveCrudRepository<Alumni,Long> {

}
