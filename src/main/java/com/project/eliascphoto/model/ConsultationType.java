package com.project.eliascphoto.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "consultation_types")
@Getter
@Setter
@NoArgsConstructor
public class ConsultationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "price_usd", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceUsd;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
