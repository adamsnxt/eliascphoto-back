package com.project.eliascphoto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;
import com.project.eliascphoto.service.JwtTokenService;

@SpringBootTest(properties = {
    "app.security.registration.allowed-ip=203.0.113.10",
    "app.appointments.minimum-notice=PT1H"
})
@AutoConfigureMockMvc
@ActiveProfiles("prod")
class AppointmentEndpointsTest {

    private static final ZoneId ARGENTINA_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    private String accessToken;

    @BeforeEach
    void setUp() {
        accessToken = jwtTokenService.createTokenPair("appointment-test-admin").getAccessToken();
    }

    @Test
    void bookingLifecycleEnforcesAccessNoticeAndNonOverlappingIntervals() throws Exception {
        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/consultation-types"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/consultation-types/manage"))
                .andExpect(status().isUnauthorized());

        String typeName = "Consulta " + UUID.randomUUID();
        String typeBody = "{\"name\":\"" + typeName + "\",\"priceUsd\":75.00,\"durationMinutes\":45}";
        mockMvc.perform(post("/api/consultation-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(typeBody))
                .andExpect(status().isUnauthorized());

        MvcResult typeResult = mockMvc.perform(post("/api/consultation-types")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(typeBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();
        Number typeIdValue = JsonPath.read(typeResult.getResponse().getContentAsString(), "$.id");
        Long typeId = typeIdValue.longValue();

        mockMvc.perform(get("/api/consultation-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + typeId + ")].priceUsd").value(75.0));

        ZonedDateTime tooSoon = ZonedDateTime.now(ARGENTINA_ZONE)
                .plusMinutes(30)
                .withSecond(0)
                .withNano(0);
        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(appointmentBody(typeId, tooSoon.toLocalDate(), tooSoon.toLocalTime())))
                .andExpect(status().isBadRequest());

        LocalDate bookingDate = LocalDate.now(ARGENTINA_ZONE)
                .plusDays(2_000 + ThreadLocalRandom.current().nextInt(100_000));
        LocalTime startTime = LocalTime.of(10, 0)
                .plusMinutes(ThreadLocalRandom.current().nextInt(0, 300));
        MvcResult firstBooking = mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(appointmentBody(typeId, bookingDate, startTime)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.priceUsd").value(75.0))
                .andExpect(jsonPath("$.durationMinutes").value(45))
                .andReturn();
        Number firstIdValue = JsonPath.read(firstBooking.getResponse().getContentAsString(), "$.id");
        Long firstId = firstIdValue.longValue();

        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(appointmentBody(typeId, bookingDate, startTime)))
                .andExpect(status().isConflict());

        LocalTime adjacentStart = startTime.plusMinutes(45);
        MvcResult adjacentBooking = mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(appointmentBody(typeId, bookingDate, adjacentStart)))
                .andExpect(status().isCreated())
                .andReturn();
        Number adjacentIdValue = JsonPath.read(adjacentBooking.getResponse().getContentAsString(), "$.id");
        Long adjacentId = adjacentIdValue.longValue();

        mockMvc.perform(put("/api/appointments/" + firstId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\":\"" + bookingDate + "\",\"startTime\":\""
                        + startTime.plusHours(3) + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"));

        mockMvc.perform(put("/api/appointments/" + firstId + "/payment-status")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"PAID\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(delete("/api/appointments/" + adjacentId)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(appointmentBody(typeId, bookingDate, adjacentStart)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/consultation-types/" + typeId)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/consultation-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + typeId + ")]").isEmpty());
        mockMvc.perform(get("/api/consultation-types/manage")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + typeId + ")].active").value(false));
        mockMvc.perform(get("/api/appointments/" + firstId)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consultationTypeName").value(typeName))
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(appointmentBody(typeId, bookingDate, startTime.plusHours(5))))
                .andExpect(status().isNotFound());
    }

    private String appointmentBody(Long typeId, LocalDate date, LocalTime startTime) {
        String unique = UUID.randomUUID().toString().replace("-", "");
        return "{\"email\":\"client-" + unique + "@example.com\","
                + "\"instagram\":\"client_" + unique.substring(0, 12) + "\","
                + "\"consultationTypeId\":" + typeId + ","
                + "\"date\":\"" + date + "\","
                + "\"startTime\":\"" + startTime + "\"}";
    }
}
