package com.example.backend.service;

import com.example.backend.domain.Drone;
import com.example.backend.repository.DroneRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DroneService {

    private final DroneRepository droneRepo;

    public DroneService(DroneRepository droneRepo) {
        this.droneRepo = droneRepo;
    }

    @Transactional
    public Drone create(Drone d) {
        
        d.setId(null);
        
        if (d.getName() == null || d.getName().isBlank()) {
            d.setName("Drone-" + System.currentTimeMillis());
        }

        if (d.getCapacityKg() <= 0) {
            d.setCapacityKg(5.0);
        }

        if (d.getRangeKm() <= 0) {
            d.setRangeKm(10.0);
        }

        if (d.getSpeedKmh() <= 0) {
            d.setSpeedKmh(30.0);
        }
        
        if (d.getStatus() == null) {
            d.setStatus(Drone.Status.IDLE);
        }
        
        if (d.getBatteryPct() < 0 || d.getBatteryPct() > 100) {
            d.setBatteryPct(100);
        }

        validate(d);
        return droneRepo.save(d);
    }

    @Transactional(readOnly = true)
    public List<Drone> listAll() {
        return droneRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Drone getById(Long id) {
        return droneRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Drone not found: " + id));
    }

    @Transactional
    public Drone updateBasicFields(Long id, String name, Double capacityKg, Double rangeKm, Double speedKmh) {
        Drone d = getById(id);

        if (name != null) d.setName(name);
        if (capacityKg != null) d.setCapacityKg(capacityKg);
        if (rangeKm != null) d.setRangeKm(rangeKm);
        if (speedKmh != null) d.setSpeedKmh(speedKmh);

        validate(d);
        return droneRepo.save(d);
    }

    @Transactional
    public void deleteIfIdle(Long id) {
        Drone d = getById(id);

        if (d.getStatus() != Drone.Status.IDLE) {
            throw new IllegalStateException("Cannot delete drone with status " + d.getStatus());
        }

        droneRepo.delete(d);
    }

    @Transactional
    public Drone updateStatus(Long id, Drone.Status status) {
        Drone d = getById(id);

        d.setStatus(status);

        return droneRepo.save(d);
    }

    @Transactional
    public Drone updateBattery(Long id, Integer batteryPct) {
        if (batteryPct == null) throw new IllegalArgumentException("batteryPct is required");

        if (batteryPct < 0 || batteryPct > 100) {
            throw new IllegalArgumentException("batteryPct must be between 0 and 100");
        }

        Drone d = getById(id);
        d.setBatteryPct(batteryPct);

        return droneRepo.save(d);
    }

    @Transactional
    public Drone updateLocation(Long id, Integer x, Integer y) {
        if (x == null || y == null) throw new IllegalArgumentException("x and y are required");

        Drone d = getById(id);
        d.setLocationX(x);
        d.setLocationY(y);
        
        return droneRepo.save(d);
    }

    private void validate(Drone d) {
        if (d.getName() == null || d.getName().isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        if (d.getCapacityKg() <= 0) {
            throw new IllegalArgumentException("capacityKg must be > 0");
        }

        if (d.getRangeKm() <= 0) {
            throw new IllegalArgumentException("rangeKm must be > 0");
        }

        if (d.getSpeedKmh() <= 0) {
            throw new IllegalArgumentException("speedKmh must be > 0");
        }
    }
}