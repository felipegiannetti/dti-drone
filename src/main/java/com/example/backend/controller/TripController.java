package com.example.backend.controller;

import com.example.backend.domain.Drone;
import com.example.backend.domain.Trip;
import com.example.backend.service.TripService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
@RestController
@RequestMapping("/trips")
public class TripController {

    private final TripService trips;

    public TripController(TripService trips) {
        this.trips = trips;
    }

    @PostMapping
    public ResponseEntity<Trip> create(@RequestBody CreateTripRequest req) {
        Trip t = new Trip();
        Drone d = new Drone();
        d.setId(Objects.requireNonNull(req.droneId, "droneId é obrigatório"));
        t.setDrone(d);

        if (req.totalWeight != null) t.setTotalWeight(req.totalWeight);
        if (req.totalDistanceKm != null) t.setTotalDistanceKm(req.totalDistanceKm);
        if (req.startAt != null) t.setStartAt(req.startAt);
        if (req.status != null) t.setStatus(req.status);

        Trip saved = trips.create(t);

        return ResponseEntity.created(URI.create("/trips/" + saved.getId())).body(saved);
    }

    /* ======================= READ ======================= */

    @GetMapping
    public List<Trip> list(
            @RequestParam(value = "status", required = false) Trip.Status status,
            @RequestParam(value = "droneId", required = false) Long droneId
    ) {
        // Implementação simples: filtra em memória a partir de listAll()
        // (Se preferir, depois expomos métodos específicos no TripService/Repository)
        List<Trip> all = trips.listAll();
        return all.stream()
                .filter(t -> status == null || t.getStatus() == status)
                .filter(t -> droneId == null || (t.getDrone() != null && droneId.equals(t.getDrone().getId())))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Trip get(@PathVariable Long id) {
        return trips.getById(id);
    }

    @PatchMapping("/{id}/status")
    public Trip updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest req) {
        return trips.updateStatus(id, req.status);
    }

    @PatchMapping("/{id}/times")
    public Trip updateTimes(@PathVariable Long id, @RequestBody UpdateTimesRequest req) {
        return trips.updateStartTime(id, req.startAt);
    }

    @PatchMapping("/{id}/totals")
    public Trip updateTotals(@PathVariable Long id, @RequestBody UpdateTotalsRequest req) {
        return trips.updateTotals(id, req.totalWeight, req.totalDistanceKm);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIfPlanned(@PathVariable Long id) {
        trips.deleteIfPlanned(id);
    }

    public static class CreateTripRequest {
        public Long droneId;
        public Double totalWeight;
        public Double totalDistanceKm;
        public Instant startAt;   
        public Trip.Status status;
    }

    public static class UpdateStatusRequest {
        public Trip.Status status;
    }

    public static class UpdateTimesRequest {
        public Instant startAt;
    }

    public static class UpdateTotalsRequest {
        public Double totalWeight;
        public Double totalDistanceKm;
    }
}
