package com.project.eliascphoto.web.dto;

import com.project.eliascphoto.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record AppointmentStatusRequest(@NotNull
        AppointmentStatus status) {

}
