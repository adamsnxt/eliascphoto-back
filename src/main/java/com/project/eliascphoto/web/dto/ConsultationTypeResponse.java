package com.project.eliascphoto.web.dto;

import java.math.BigDecimal;

public record ConsultationTypeResponse(
        Long id,
        String name,
        BigDecimal priceUsd,
        int durationMinutes,
        boolean active) {

}
