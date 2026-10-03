package com.project.eliascphoto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;
import com.project.eliascphoto.model.Review;
import com.project.eliascphoto.repository.ReviewRepository;

@SpringBootTest(properties = "app.security.registration.allowed-ip=203.0.113.10")
@AutoConfigureMockMvc
@ActiveProfiles("prod")
class AuthEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    void swaggerIsPublicAndUsersApiRequiresAnAccessToken() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerAndLoginNeedNoTokenAndRegistrationChecksIp() throws Exception {
        String userName = "test-" + java.util.UUID.randomUUID();
        String password = "password-for-test";
        String registrationBody = "{\"userName\":\"" + userName + "\",\"password\":\"" + password + "\"}";

        mockMvc.perform(post("/api/auth/register")
                .header("X-Forwarded-For", "198.51.100.20, 203.0.113.10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrationBody))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrationBody))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        String accessToken = JsonPath.read(loginResponse, "$.accessToken");
        String refreshToken = JsonPath.read(loginResponse, "$.refreshToken");

        mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized());

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();
        String refreshedAccessToken = JsonPath.read(
                refreshResult.getResponse().getContentAsString(), "$.accessToken");
        String refreshedRefreshToken = JsonPath.read(
                refreshResult.getResponse().getContentAsString(), "$.refreshToken");
        mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + refreshedAccessToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + accessToken + "\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshedRefreshToken + "\"}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshedRefreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + refreshedAccessToken))
                .andExpect(status().isOk());

        String blockedBody = "{\"userName\":\"blocked-" + userName + "\",\"password\":\"" + password + "\"}";
        mockMvc.perform(post("/api/auth/register")
                .header("X-Forwarded-For", "198.51.100.20, 198.51.100.20")
                .contentType(MediaType.APPLICATION_JSON)
                .content(blockedBody))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"" + userName + "\",\"password\":\"incorrect-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void reviewsSupportPublicCrudWithoutBearerToken() throws Exception {
        String createBody = "{\"name\":\"Elena\",\"text\":\"Excelente experiencia\",\"rate\":4.5}";
        MvcResult createResult = mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isActive").value(true))
                .andReturn();

        Number reviewIdValue = com.jayway.jsonpath.JsonPath.read(
                createResult.getResponse().getContentAsString(), "$.id");
        long reviewId = reviewIdValue.longValue();

        Review inactiveReview = new Review();
        inactiveReview.setName("Hidden " + java.util.UUID.randomUUID());
        inactiveReview.setText("No debe aparecer en GET");
        inactiveReview.setRate(3.0f);
        inactiveReview.setActive(false);
        Review savedInactiveReview = reviewRepository.save(inactiveReview);

        MvcResult reviewsResult = mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andReturn();
        String reviewsJson = reviewsResult.getResponse().getContentAsString();
        java.util.List<String> reviewNames = JsonPath.read(reviewsJson, "$[*].name");
        assertTrue(reviewNames.contains("Elena"));
        assertFalse(reviewNames.contains(savedInactiveReview.getName()));
        mockMvc.perform(get("/api/reviews/" + reviewId))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/reviews/" + savedInactiveReview.getId()))
                .andExpect(status().isNotFound());

        String updateBody = "{\"name\":\"Elena Updated\",\"text\":\"Muy buena experiencia\",\"rate\":5.0}";
        mockMvc.perform(put("/api/reviews/" + reviewId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));

        mockMvc.perform(delete("/api/reviews/" + reviewId))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/reviews/" + reviewId))
                .andExpect(status().isNotFound());
    }
}
