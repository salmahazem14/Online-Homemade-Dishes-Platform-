package com.example.demo.repository;

import com.example.demo.model.Customer;
import org.apache.logging.log4j.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long>{
}