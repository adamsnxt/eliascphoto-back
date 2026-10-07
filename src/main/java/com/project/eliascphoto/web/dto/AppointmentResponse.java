package com.project.eliascphoto.web.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.project.eliascphoto.model.AppointmentStatus;

public record AppointmentResponse(
        Long id,
        String email,
        String instagram,
        Long consultationTypeId,
        String consultationTypeName,
        BigDecimal priceUsd,
        int durationMinutes,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        AppointmentStatus status) {

}
