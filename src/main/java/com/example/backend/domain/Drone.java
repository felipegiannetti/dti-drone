package com.example.backend.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "drones")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Drone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false)
    private double capacityKg;

    @Column(nullable = false)
    private double rangeKm;

    @Column(nullable = false)
    private double speedKmh = 40.0;

    @Column(nullable = false)
    private int batteryPct = 100;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.IDLE;

    @Column(nullable = false)
    private int locationX = 0;

    @Column(nullable = false)
    private int locationY = 0;

    public enum Status {
        IDLE, CARREGANDO, EM_VOO, ENTREGANDO, RETORNANDO
    }
}