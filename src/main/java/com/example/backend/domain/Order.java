package com.example.backend.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int customerX;

    @Column(nullable = false)
    private int customerY;

    @Column(nullable = false)
    private double weightKg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Status status = Status.PENDING;

    public enum Priority { LOW, MEDIUM, HIGH }
    public enum Status { PENDING, PLANNED, DELIVERED, REJECTED }
}
