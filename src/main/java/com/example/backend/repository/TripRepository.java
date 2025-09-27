package com.example.backend.repository;

import com.example.backend.domain.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByStatus(Trip.Status status);

    /* Buscas do mais recente para o mais antigo */
    List<Trip> findByDroneIdOrderByStartAtDesc(Long droneId);
    Optional<Trip> findFirstByDroneIdAndStatusOrderByStartAtDesc(Long droneId, Trip.Status status);
}
