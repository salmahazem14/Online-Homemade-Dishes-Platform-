package com.example.demo.repository;
import com.example.demo.model.Dummy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DummyRepository extends JpaRepository<Dummy, Long> {
    Dummy save(Dummy dummy);

    List<Dummy> findAll();
}
