package com.project.eliascphoto.web.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateAppointmentRequest(
        @NotBlank
        @Email
        @Size(max = 254)
        String email,
        @NotBlank
        @Size(max = 100)
        String instagram,
        @NotNull
        @Positive
        Long consultationTypeId,
        @NotNull
        LocalDate date,
        @NotNull
        LocalTime startTime) {

}
