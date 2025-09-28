package com.example.backend.service;

import com.example.backend.domain.Drone;
import com.example.backend.domain.Trip;
import com.example.backend.repository.DroneRepository;
import com.example.backend.repository.TripRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class TripService {

    private final TripRepository tripRepo;
    private final DroneRepository droneRepo;

    public TripService(TripRepository tripRepo, DroneRepository droneRepo) {
        this.tripRepo = tripRepo;
        this.droneRepo = droneRepo;
    }

    @Transactional
    public Trip create(Trip t) {
        t.setId(null);

        if (t.getDrone() == null || t.getDrone().getId() == null) {
            throw new IllegalArgumentException("drone com ID é obrigatório para criar Trip");
        }

        Drone drone = droneRepo.findById(t.getDrone().getId())
                .orElseThrow(() -> new EntityNotFoundException("Drone não encontrado: " + t.getDrone().getId()));

        t.setDrone(drone);

        if (t.getTotalWeight() <= 0) {
            t.setTotalWeight(0.0);
        }

        if (t.getTotalDistanceKm() < 0) {
            t.setTotalDistanceKm(0.0);
        }

        if (t.getStartAt() == null) {
            t.setStartAt(Instant.now());
        }

        if (t.getStatus() == null) {
            t.setStatus(Trip.Status.PLANNED);
        }

        t.setFinishAt(null);

        validate(t);

        return tripRepo.save(t);
    }

    @Transactional(readOnly = true)
    public List<Trip> listAll() {
        return tripRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Trip getById(Long id) {
        return tripRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trip não encontrada: " + id));
    }

    @Transactional
    public void deleteIfPlanned(Long id) {
        Trip trip = getById(id);

        if (trip.getStatus() != Trip.Status.PLANNED) {
            throw new IllegalStateException("Só é permitido excluir Trip com status PLANNED");
        }
        
        tripRepo.delete(trip);
    }

    @Transactional
    public Trip updateStatus(Long id, Trip.Status newStatus) {
        Trip t = getById(id);
        t.setStatus(newStatus);

        validate(t);

        return tripRepo.save(t);
    }

    @Transactional
    public Trip updateStartTime(Long id, Instant startAt) {
        Trip t = getById(id);

        if (startAt != null) {
            t.setStartAt(startAt);
            
            if (t.getTotalDistanceKm() > 0 && t.getDrone().getSpeedKmh() > 0) {
                double durationHours = t.getTotalDistanceKm() / t.getDrone().getSpeedKmh();
                long durationSeconds = (long) (durationHours * 3600);

                t.setFinishAt(startAt.plusSeconds(durationSeconds));
            }
        }

        validate(t);
        
        return tripRepo.save(t);
    }    
    
    @Transactional
    public Trip updateTotals(Long id, Double totalWeight, Double totalDistanceKm) {
        Trip t = getById(id);

        if (totalWeight != null) t.setTotalWeight(totalWeight);

        if (totalDistanceKm != null) {
            t.setTotalDistanceKm(totalDistanceKm);
            
            if (t.getStartAt() != null && totalDistanceKm > 0 && t.getDrone().getSpeedKmh() > 0) {
                double durationHours = totalDistanceKm / t.getDrone().getSpeedKmh();
                long durationSeconds = (long) (durationHours * 3600);

                t.setFinishAt(t.getStartAt().plusSeconds(durationSeconds));
            }
        }
        
        if (t.getTotalWeight() < 0) t.setTotalWeight(0.0);
        if (t.getTotalDistanceKm() < 0) t.setTotalDistanceKm(0.0);

        validate(t);

        return tripRepo.save(t);
    }

    private void validate(Trip t) {
        if (t.getDrone() == null) {
            throw new IllegalArgumentException("Trip sem drone");
        }

        if (t.getTotalWeight() < 0) {
            throw new IllegalArgumentException("totalWeight não pode ser negativo");
        }

        if (t.getTotalDistanceKm() < 0) {
            throw new IllegalArgumentException("totalDistanceKm não pode ser negativo");
        }
        
        if (t.getStatus() == null) {
            throw new IllegalArgumentException("status não pode ser nulo");
        }
    }
}
