package com.project.eliascphoto.web.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

public record RescheduleAppointmentRequest(
        @NotNull
        LocalDate date,
        @NotNull
        LocalTime startTime) {

}
