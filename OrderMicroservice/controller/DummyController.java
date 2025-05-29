package com.example.demo.controller;

import com.example.demo.model.dummy;
import com.example.demo.repository.DummyRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dummy")
public class DummyController {

    private final DummyRepository dummyRepository;

    public DummyController(DummyRepository dummyRepository) {
        this.dummyRepository = dummyRepository;
    }

    @GetMapping
    public List<dummy> getAll() {
        return dummyRepository.findAll();
    }

    @PostMapping
    public dummy create(@RequestBody dummy dummy) {
        return dummyRepository.save(dummy);
    }
}
