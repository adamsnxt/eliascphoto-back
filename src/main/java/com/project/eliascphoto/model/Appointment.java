package com.project.eliascphoto.model;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "instagram_handle", nullable = false, length = 100)
    private String instagram;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consultation_type_id", nullable = false)
    private ConsultationType consultationType;

    @Column(name = "consultation_name_snapshot", nullable = false, length = 100)
    private String consultationNameSnapshot;

    @Column(name = "price_usd_snapshot", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceUsdSnapshot;

    @Column(name = "duration_minutes_snapshot", nullable = false)
    private int durationMinutesSnapshot;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private AppointmentStatus status = AppointmentStatus.PENDING_PAYMENT;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;
}
