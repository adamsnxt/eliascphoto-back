package com.project.eliascphoto.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.eliascphoto.model.Appointment;
import com.project.eliascphoto.model.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findAllByOrderByStartAtAsc();

    List<Appointment> findAllByStatusNotAndStartAtBeforeAndEndAtAfter(
            AppointmentStatus excludedStatus,
            Instant candidateEnd,
            Instant candidateStart);

    @Query("SELECT appointment FROM Appointment appointment "
            + "WHERE appointment.status <> :cancelled "
            + "AND appointment.id <> :excludedId "
            + "AND appointment.startAt < :candidateEnd "
            + "AND appointment.endAt > :candidateStart")
    List<Appointment> findOverlappingAppointmentsExcept(
            @Param("cancelled") AppointmentStatus cancelled,
            @Param("excludedId") Long excludedId,
            @Param("candidateEnd") Instant candidateEnd,
            @Param("candidateStart") Instant candidateStart);
}
