package com.project.eliascphoto.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.project.eliascphoto.model.ConsultationType;
import com.project.eliascphoto.repository.ConsultationTypeRepository;
import com.project.eliascphoto.web.dto.ConsultationTypeRequest;
import com.project.eliascphoto.web.dto.ConsultationTypeResponse;

@Service
@Transactional
public class ConsultationTypeService {

    private final ConsultationTypeRepository consultationTypeRepository;

    public ConsultationTypeService(ConsultationTypeRepository consultationTypeRepository) {
        this.consultationTypeRepository = consultationTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<ConsultationTypeResponse> getActiveTypes() {
        return consultationTypeRepository.findAllByActiveTrueOrderByNameAsc().stream()
                .map(ConsultationTypeService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConsultationTypeResponse> getAllTypes() {
        return consultationTypeRepository.findAllByOrderByNameAsc().stream()
                .map(ConsultationTypeService::toResponse)
                .toList();
    }

    public ConsultationTypeResponse createType(ConsultationTypeRequest request) {
        ConsultationType type = new ConsultationType();
        applyRequest(type, request);
        type.setActive(request.active() == null || request.active());
        return toResponse(consultationTypeRepository.save(type));
    }

    public ConsultationTypeResponse updateType(Long id, ConsultationTypeRequest request) {
        ConsultationType type = findType(id);
        applyRequest(type, request);
        if (request.active() != null) {
            type.setActive(request.active());
        }
        return toResponse(consultationTypeRepository.save(type));
    }

    public void deactivateType(Long id) {
        ConsultationType type = findType(id);
        type.setActive(false);
        consultationTypeRepository.save(type);
    }

    private ConsultationType findType(Long id) {
        return consultationTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de asesoria no encontrado"));
    }

    private void applyRequest(ConsultationType type, ConsultationTypeRequest request) {
        type.setName(request.name().trim());
        type.setPriceUsd(request.priceUsd());
        type.setDurationMinutes(request.durationMinutes());
    }

    private static ConsultationTypeResponse toResponse(ConsultationType type) {
        return new ConsultationTypeResponse(
                type.getId(), type.getName(), type.getPriceUsd(), type.getDurationMinutes(), type.isActive());
    }
}
