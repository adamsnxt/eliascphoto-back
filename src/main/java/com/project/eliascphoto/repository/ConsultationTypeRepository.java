package com.project.eliascphoto.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.eliascphoto.model.ConsultationType;

public interface ConsultationTypeRepository extends JpaRepository<ConsultationType, Long> {

    List<ConsultationType> findAllByActiveTrueOrderByNameAsc();

    Optional<ConsultationType> findByIdAndActiveTrue(Long id);

    List<ConsultationType> findAllByOrderByNameAsc();
}
