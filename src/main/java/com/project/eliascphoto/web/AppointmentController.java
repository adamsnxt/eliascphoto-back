package com.project.eliascphoto.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.eliascphoto.service.AppointmentService;
import com.project.eliascphoto.web.dto.AppointmentResponse;
import com.project.eliascphoto.web.dto.AppointmentStatusRequest;
import com.project.eliascphoto.web.dto.CreateAppointmentRequest;
import com.project.eliascphoto.web.dto.RescheduleAppointmentRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<AppointmentResponse> getAppointments() {
        return appointmentService.getAppointments();
    }

    @GetMapping("/{id}")
    public AppointmentResponse getAppointment(@PathVariable Long id) {
        return appointmentService.getAppointment(id);
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.createAppointment(request));
    }

    @PutMapping("/{id}")
    public AppointmentResponse reschedule(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleAppointmentRequest request) {
        return appointmentService.reschedule(id, request);
    }

    @PutMapping("/{id}/payment-status")
    public AppointmentResponse updatePaymentStatus(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusRequest request) {
        if (request.status() != com.project.eliascphoto.model.AppointmentStatus.PAID) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El estado solo puede actualizarse a PAID por esta ruta");
        }
        return appointmentService.updatePaymentStatus(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        appointmentService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
