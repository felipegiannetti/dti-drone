package com.example.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
    name = "trip_stops",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_trip_seq", columnNames = {"trip_id", "seq"})
    },
    indexes = {
        @Index(name = "idx_trip_stops_trip", columnList = "trip_id"),
        @Index(name = "idx_trip_stops_order", columnList = "order_id")
    }
)
@Data
@NoArgsConstructor
public class TripStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    @JsonIgnore
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Ordem de atendimento dentro da viagem (1, 2, 3, ...)
    @Column(nullable = false)
    private int seq;

    @Column(nullable = false)
    private int x;

    @Column(nullable = false)
    private int y;

    // (estimativa ou real, conforme seu fluxo)
    private Instant estimatedArrivalAt;
    private Instant estimatedDepartureAt;

    @Column(nullable = false)
    private boolean delivered = false;
    
    // Método temporário devido a problema com Lombok + JsonIgnore
    public void setTrip(Trip trip) {
        this.trip = trip;
    }
}
