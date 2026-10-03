package com.project.eliascphoto.web;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.project.eliascphoto.service.JwtTokenPair;
import com.project.eliascphoto.service.RegistrationIpGuard;
import com.project.eliascphoto.service.UserService;
import com.project.eliascphoto.web.dto.CreateUserRequest;
import com.project.eliascphoto.web.dto.LoginRequest;
import com.project.eliascphoto.web.dto.RefreshTokenRequest;
import com.project.eliascphoto.web.dto.UserResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final RegistrationIpGuard registrationIpGuard;

    public AuthController(UserService userService, RegistrationIpGuard registrationIpGuard) {
        this.userService = userService;
        this.registrationIpGuard = registrationIpGuard;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody CreateUserRequest request,
            HttpServletRequest servletRequest) {
        String registrationId = UUID.randomUUID().toString();
        LOGGER.info("Registration id={} stage=request_received", registrationId);
        try {
            registrationIpGuard.requireAllowed(servletRequest, registrationId);
            UserResponse response = userService.registerUser(request, registrationId);
            LOGGER.info("Registration id={} stage=completed status={}", registrationId, HttpStatus.CREATED.value());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResponseStatusException exception) {
            LOGGER.warn("Registration id={} stage=rejected status={}",
                    registrationId, exception.getStatusCode().value());
            throw exception;
        } catch (RuntimeException exception) {
            LOGGER.error("Registration id={} stage=failed", registrationId, exception);
            throw exception;
        }
    }

    @PostMapping("/login")
    public JwtTokenPair login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request.getUserName(), request.getPassword());
    }

    @PostMapping("/refresh")
    public JwtTokenPair refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return userService.refresh(request.getRefreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        userService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
