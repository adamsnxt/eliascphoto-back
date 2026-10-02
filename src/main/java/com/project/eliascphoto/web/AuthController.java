package com.project.eliascphoto.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.eliascphoto.service.JwtTokenPair;
import com.project.eliascphoto.service.RegistrationIpGuard;
import com.project.eliascphoto.service.UserService;
import com.project.eliascphoto.web.dto.CreateUserRequest;
import com.project.eliascphoto.web.dto.LoginRequest;
import com.project.eliascphoto.web.dto.UserResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

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
        registrationIpGuard.requireAllowed(servletRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(request));
    }

    @PostMapping("/login")
    public JwtTokenPair login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request.getUserName(), request.getPassword());
    }
}
