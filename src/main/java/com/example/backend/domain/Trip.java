package com.example.backend.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trips")
@Data
@NoArgsConstructor
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "drone_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Drone drone;

    @Column(nullable = false)
    private double totalWeight;

    @Column(nullable = false)
    private double totalDistanceKm;

    @Column(nullable = false)
    private Instant startAt = Instant.now();

    @Column
    private Instant finishAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status = Status.PLANNED;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("seq ASC")
    private List<TripStop> stops = new ArrayList<>();

    public enum Status { PLANNED, IN_PROGRESS, FINISHED }

    public void addStop(TripStop stop) {
        stops.add(stop);
        stop.setTrip(this);
    }

    public void removeStop(TripStop stop) {
        stops.remove(stop);
        stop.setTrip(null); // Quebra a referência do TripStop para a Trip
    }
}
