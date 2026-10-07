package com.project.eliascphoto.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ConsultationTypeRequest(
        @NotBlank
        @Size(max = 100)
        String name,
        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 10, fraction = 2)
        BigDecimal priceUsd,
        @NotNull
        @Min(1)
        Integer durationMinutes,
        Boolean active) {

}
