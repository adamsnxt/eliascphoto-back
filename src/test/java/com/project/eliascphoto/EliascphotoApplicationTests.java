package com.project.eliascphoto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.eliascphoto.service.JwtTokenPair;
import com.project.eliascphoto.service.JwtTokenService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EliascphotoApplicationTests {

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void createsAndReadsAccessAndRefreshTokens() {
        JwtTokenPair tokens = jwtTokenService.createTokenPair("token-test-user");

        assertEquals("token-test-user", jwtTokenService.readAccessToken(tokens.getAccessToken()).getSubject());
        assertEquals("token-test-user", jwtTokenService.readRefreshToken(tokens.getRefreshToken()).getSubject());
    }

    @Test
    void registrationDoesNotRequireIpAllowlistOutsideProduction() throws Exception {
        String userName = "local-test-" + java.util.UUID.randomUUID();
        String body = "{\"userName\":\"" + userName + "\",\"password\":\"local-test-password\"}";

        mockMvc.perform(post("/api/auth/register")
                .header("X-Forwarded-For", "192.0.2.123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated());
    }

}
