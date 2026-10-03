package com.project.eliascphoto.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @NotBlank
        @Size(max = 100)
        String name,
        @NotBlank
        @Size(max = 3000)
        String text,
        @DecimalMin("1.0")
        @DecimalMax("5.0")
        float rate,
        Boolean isActive) {

}
