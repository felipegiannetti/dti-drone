package com.example.backend.controller;

import com.example.backend.domain.TripStop;
import com.example.backend.service.TripStopService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
@RestController
@RequestMapping("/trips/{tripId}/stops")
public class TripStopController {

    private final TripStopService stops;

    public TripStopController(TripStopService stops) {
        this.stops = stops;
    }

    @GetMapping
    public List<TripStop> list(@PathVariable Long tripId) {
        return stops.listByTrip(tripId);
    }

    @GetMapping("/{seq}")
    public TripStop get(@PathVariable Long tripId, @PathVariable int seq) {
        return stops.getByTripAndSeq(tripId, seq);
    }

    @PostMapping
    public ResponseEntity<TripStop> create(@PathVariable Long tripId, @RequestBody CreateStopRequest req) {
        TripStop saved = stops.create(tripId,req.orderId,req.x,req.y,req.seq);

        return ResponseEntity
                .created(URI.create("/trips/" + tripId + "/stops/" + saved.getSeq()))
                .body(saved);
    }

    @PatchMapping("/{seq}/move")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void move(@PathVariable Long tripId, @PathVariable int seq, @RequestBody MoveRequest req) {
        stops.move(tripId, seq, req.toSeq);
    }

    @PatchMapping("/{seq}/delivered")
    public TripStop markDelivered(@PathVariable Long tripId,@PathVariable int seq) {
        return stops.markDelivered(tripId, seq);
    }

    @PatchMapping("/{seq}/estimates")
    public TripStop updateEstimates(@PathVariable Long tripId, @PathVariable int seq, @RequestBody UpdateEstimatesRequest req) {
        return stops.updateEstimates(tripId, seq, req.estimatedArrivalAt, req.estimatedDepartureAt);
    }

    @DeleteMapping("/{seq}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIfPlanned(@PathVariable Long tripId, @PathVariable int seq) {
        stops.deleteIfPlanned(tripId, seq);
    }

    public static class CreateStopRequest {
        public Long orderId;
        public Integer x;
        public Integer y;
        public Integer seq;
    }

    public static class MoveRequest {
        public int toSeq;
    }

    public static class UpdateEstimatesRequest {
        public Instant estimatedArrivalAt;
        public Instant estimatedDepartureAt;
    }
}