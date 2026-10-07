package com.project.eliascphoto.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.project.eliascphoto.config.AppointmentProperties;
import com.project.eliascphoto.model.Appointment;
import com.project.eliascphoto.model.AppointmentStatus;
import com.project.eliascphoto.model.ConsultationType;
import com.project.eliascphoto.repository.AppointmentRepository;
import com.project.eliascphoto.repository.AppointmentScheduleLockRepository;
import com.project.eliascphoto.repository.ConsultationTypeRepository;
import com.project.eliascphoto.web.dto.AppointmentResponse;
import com.project.eliascphoto.web.dto.CreateAppointmentRequest;
import com.project.eliascphoto.web.dto.RescheduleAppointmentRequest;

@Service
@Transactional
public class AppointmentService {

    private static final long SCHEDULE_LOCK_ID = 1L;

    private final AppointmentRepository appointmentRepository;
    private final AppointmentScheduleLockRepository scheduleLockRepository;
    private final ConsultationTypeRepository consultationTypeRepository;
    private final AppointmentProperties properties;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            AppointmentScheduleLockRepository scheduleLockRepository,
            ConsultationTypeRepository consultationTypeRepository,
            AppointmentProperties properties) {
        this.appointmentRepository = appointmentRepository;
        this.scheduleLockRepository = scheduleLockRepository;
        this.consultationTypeRepository = consultationTypeRepository;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointments() {
        return appointmentRepository.findAllByOrderByStartAtAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointment(Long id) {
        return toResponse(findAppointment(id));
    }

    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        lockSchedule();
        ConsultationType type = consultationTypeRepository.findByIdAndActiveTrue(request.consultationTypeId())
                .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Tipo de asesoria no encontrado o inactivo"));

        Instant startAt = resolveStart(request.date(), request.startTime());
        requireMinimumNotice(startAt);
        Instant endAt = startAt.plusSeconds(type.getDurationMinutes() * 60L);
        requireNoOverlap(startAt, endAt, null);

        Appointment appointment = new Appointment();
        appointment.setEmail(request.email().trim());
        appointment.setInstagram(request.instagram().trim());
        appointment.setConsultationType(type);
        appointment.setConsultationNameSnapshot(type.getName());
        appointment.setPriceUsdSnapshot(type.getPriceUsd());
        appointment.setDurationMinutesSnapshot(type.getDurationMinutes());
        appointment.setStartAt(startAt);
        appointment.setEndAt(endAt);
        appointment.setStatus(AppointmentStatus.PENDING_PAYMENT);
        return toResponse(appointmentRepository.save(appointment));
    }

    public AppointmentResponse reschedule(Long id, RescheduleAppointmentRequest request) {
        lockSchedule();
        Appointment appointment = findAppointment(id);
        requireNotCancelled(appointment);

        Instant startAt = resolveStart(request.date(), request.startTime());
        requireMinimumNotice(startAt);
        Instant endAt = startAt.plusSeconds(appointment.getDurationMinutesSnapshot() * 60L);
        requireNoOverlap(startAt, endAt, id);

        appointment.setStartAt(startAt);
        appointment.setEndAt(endAt);
        return toResponse(appointmentRepository.save(appointment));
    }

    public AppointmentResponse updatePaymentStatus(Long id) {
        lockSchedule();
        Appointment appointment = findAppointment(id);
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede cobrar un turno cancelado");
        }
        if (appointment.getStatus() != AppointmentStatus.PAID) {
            appointment.setStatus(AppointmentStatus.PAID);
            appointment.setPaidAt(Instant.now());
        }
        return toResponse(appointmentRepository.save(appointment));
    }

    public void cancel(Long id) {
        lockSchedule();
        Appointment appointment = findAppointment(id);
        if (appointment.getStatus() != AppointmentStatus.CANCELLED) {
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointment.setCancelledAt(Instant.now());
            appointmentRepository.save(appointment);
        }
    }

    private void lockSchedule() {
        if (scheduleLockRepository.lockById(SCHEDULE_LOCK_ID) == null) {
            throw new IllegalStateException("No existe el bloqueo de agenda");
        }
    }

    private Instant resolveStart(java.time.LocalDate date, java.time.LocalTime time) {
        LocalDateTime localDateTime = LocalDateTime.of(date, time);
        List<ZoneOffset> validOffsets = properties.zoneId().getRules().getValidOffsets(localDateTime);
        if (validOffsets.size() != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La hora local no es valida en la zona configurada");
        }
        return localDateTime.toInstant(validOffsets.get(0));
    }

    private void requireMinimumNotice(Instant startAt) {
        if (startAt.isBefore(Instant.now().plus(properties.getMinimumNotice()))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los turnos deben solicitarse con al menos " + properties.getMinimumNotice().toMinutes() + " minutos de anticipacion");
        }
    }

    private void requireNoOverlap(Instant startAt, Instant endAt, Long excludedId) {
        boolean overlaps = excludedId == null
                ? !appointmentRepository.findAllByStatusNotAndStartAtBeforeAndEndAtAfter(
                        AppointmentStatus.CANCELLED, endAt, startAt).isEmpty()
                : !appointmentRepository.findOverlappingAppointmentsExcept(
                        AppointmentStatus.CANCELLED, excludedId, endAt, startAt).isEmpty();
        if (overlaps) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese horario ya esta ocupado");
        }
    }

    private void requireNotCancelled(Appointment appointment) {
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El turno esta cancelado");
        }
    }

    private Appointment findAppointment(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turno no encontrado"));
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getEmail(),
                appointment.getInstagram(),
                appointment.getConsultationType().getId(),
                appointment.getConsultationNameSnapshot(),
                appointment.getPriceUsdSnapshot(),
                appointment.getDurationMinutesSnapshot(),
                OffsetDateTime.ofInstant(appointment.getStartAt(), properties.zoneId()),
                OffsetDateTime.ofInstant(appointment.getEndAt(), properties.zoneId()),
                appointment.getStatus());
    }
}
