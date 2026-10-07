package com.project.eliascphoto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.eliascphoto.model.AppointmentScheduleLock;

import jakarta.persistence.LockModeType;

public interface AppointmentScheduleLockRepository extends JpaRepository<AppointmentScheduleLock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT scheduleLock FROM AppointmentScheduleLock scheduleLock WHERE scheduleLock.id = :id")
    AppointmentScheduleLock lockById(@Param("id") Long id);
}
