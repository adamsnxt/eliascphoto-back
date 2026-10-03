package com.project.eliascphoto.service;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.project.eliascphoto.model.RefreshTokenSession;
import com.project.eliascphoto.model.User;
import com.project.eliascphoto.repository.RefreshTokenSessionRepository;
import com.project.eliascphoto.repository.UserRepository;
import com.project.eliascphoto.web.dto.CreateUserRequest;
import com.project.eliascphoto.web.dto.UserResponse;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public UserService(
            UserRepository userRepository,
            RefreshTokenSessionRepository refreshTokenSessionRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponse(user.getUserName()))
                .toList();
    }

    public UserResponse registerUser(CreateUserRequest request) {
        String userName = request.getUserName().trim();
        if (userRepository.existsByUserName(userName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario ya existe");
        }

        User user = new User();
        user.setUserName(userName);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        return new UserResponse(savedUser.getUserName());
    }

    public JwtTokenPair login(String userName, String password) {
        User user = userRepository.findByUserName(userName.trim())
                .filter(existingUser -> passwordEncoder.matches(password, existingUser.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos"));

        JwtTokenPair tokens = jwtTokenService.createTokenPair(user.getUserName());
        saveRefreshTokenSession(tokens, user.getUserName());
        return tokens;
    }

    public JwtTokenPair refresh(String refreshToken) {
        Jwt refreshJwt = readRefreshTokenOrUnauthorized(refreshToken);
        String userName = refreshJwt.getSubject();
        Instant now = Instant.now();
        if (!userRepository.existsByUserName(userName)
                || refreshTokenSessionRepository.revokeIfActive(refreshJwt.getId(), userName, now) != 1) {
            throw invalidRefreshToken();
        }

        JwtTokenPair tokens = jwtTokenService.createTokenPair(userName);
        saveRefreshTokenSession(tokens, userName);
        return tokens;
    }

    public void logout(String refreshToken) {
        try {
            Jwt refreshJwt = jwtTokenService.readRefreshToken(refreshToken);
            refreshTokenSessionRepository.revokeIfActive(
                    refreshJwt.getId(), refreshJwt.getSubject(), Instant.now());
        } catch (JwtException ignored) {
        }
    }

    private Jwt readRefreshTokenOrUnauthorized(String refreshToken) {
        try {
            return jwtTokenService.readRefreshToken(refreshToken);
        } catch (JwtException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido", exception);
        }
    }

    private void saveRefreshTokenSession(JwtTokenPair tokens, String userName) {
        Instant now = Instant.now();
        refreshTokenSessionRepository.deleteByExpiresAtBefore(now);

        Jwt refreshJwt = jwtTokenService.readRefreshToken(tokens.getRefreshToken());
        RefreshTokenSession session = new RefreshTokenSession();
        session.setTokenId(refreshJwt.getId());
        session.setUserName(userName);
        session.setExpiresAt(refreshJwt.getExpiresAt());
        refreshTokenSessionRepository.save(session);
    }

    private ResponseStatusException invalidRefreshToken() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido");
    }
}
