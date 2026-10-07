package com.project.eliascphoto.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "appointment_schedule_locks")
@Getter
@NoArgsConstructor
public class AppointmentScheduleLock {

    @Id
    private Long id;
}
