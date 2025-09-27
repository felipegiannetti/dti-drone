package com.example.backend.controller;

import com.example.backend.domain.Drone;
import com.example.backend.service.DroneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
@RestController
@RequestMapping("/drones")
public class DroneController {

    private final DroneService drones;

    public DroneController(DroneService drones) {
        this.drones = drones;
    }

    @PostMapping
    public ResponseEntity<Drone> create(@RequestBody Drone body) {
        Drone saved = drones.create(body);
        return ResponseEntity
                .created(URI.create("/drones/" + saved.getId()))
                .body(saved);
    }

    @GetMapping
    public List<Drone> list() {
        return drones.listAll();
    }

    @GetMapping("/{id}")
    public Drone get(@PathVariable Long id) {
        return drones.getById(id);
    }

    @PatchMapping("/{id}")
    public Drone updateBasic(@PathVariable Long id, @RequestBody UpdateDroneRequest req) {
        return drones.updateBasicFields(id, req.name, req.capacityKg, req.rangeKm, req.speedKmh);
    }

    @PatchMapping("/{id}/status")
    public Drone updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest req) {
        return drones.updateStatus(id, req.status);
    }

    @PatchMapping("/{id}/battery")
    public Drone updateBattery(@PathVariable Long id, @RequestBody UpdateBatteryRequest req) {
        return drones.updateBattery(id, req.batteryPct);
    }

    @PatchMapping("/{id}/location")
    public Drone updateLocation(@PathVariable Long id, @RequestBody UpdateLocationRequest req) {
        return drones.updateLocation(id, req.x, req.y);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIfIdle(@PathVariable Long id) {
        drones.deleteIfIdle(id);
    }

    public static class UpdateDroneRequest {
        public String name;
        public Double capacityKg;
        public Double rangeKm;
        public Double speedKmh;
    }

    public static class UpdateStatusRequest {
        public Drone.Status status;
    }

    public static class UpdateBatteryRequest {
        public Integer batteryPct;
    }

    public static class UpdateLocationRequest {
        public Integer x;
        public Integer y;
    }
}
