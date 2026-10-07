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

import com.project.eliascphoto.service.ConsultationTypeService;
import com.project.eliascphoto.web.dto.ConsultationTypeRequest;
import com.project.eliascphoto.web.dto.ConsultationTypeResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/consultation-types")
public class ConsultationTypeController {

    private final ConsultationTypeService consultationTypeService;

    public ConsultationTypeController(ConsultationTypeService consultationTypeService) {
        this.consultationTypeService = consultationTypeService;
    }

    @GetMapping
    public List<ConsultationTypeResponse> getActiveTypes() {
        return consultationTypeService.getActiveTypes();
    }

    @GetMapping("/manage")
    public List<ConsultationTypeResponse> getAllTypes() {
        return consultationTypeService.getAllTypes();
    }

    @PostMapping
    public ResponseEntity<ConsultationTypeResponse> createType(
            @Valid @RequestBody ConsultationTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consultationTypeService.createType(request));
    }

    @PutMapping("/{id}")
    public ConsultationTypeResponse updateType(
            @PathVariable Long id,
            @Valid @RequestBody ConsultationTypeRequest request) {
        return consultationTypeService.updateType(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateType(@PathVariable Long id) {
        consultationTypeService.deactivateType(id);
        return ResponseEntity.noContent().build();
    }
}
