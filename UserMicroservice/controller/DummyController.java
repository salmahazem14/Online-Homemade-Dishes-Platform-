package com.example.demo.controller;

import com.example.demo.model.Dummy;
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
    public List<Dummy> getAll() {
        return dummyRepository.findAll();
    }

    @PostMapping
    public Dummy create(@RequestBody Dummy dummy) {
        return dummyRepository.save(dummy);
    }
}
