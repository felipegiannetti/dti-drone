package com.example.backend.controller;

import com.example.backend.domain.Trip;
import com.example.backend.service.PlanningService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
@RestController
@RequestMapping("/plan")
public class PlanController {

    private final PlanningService planning;

    public PlanController(PlanningService planning) {
        this.planning = planning;
    }

    @PostMapping
    public List<Trip> plan() {
        return planning.planAll();
    }
}
