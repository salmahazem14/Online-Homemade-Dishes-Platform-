package com.example.demo.repository;

import com.example.demo.model.dummy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DummyRepository extends JpaRepository<dummy, Long> {
}
