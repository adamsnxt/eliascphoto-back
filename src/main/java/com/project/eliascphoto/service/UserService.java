package com.project.eliascphoto.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.project.eliascphoto.model.User;
import com.project.eliascphoto.repository.UserRepository;
import com.project.eliascphoto.web.dto.CreateUserRequest;
import com.project.eliascphoto.web.dto.UserResponse;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
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

    @Transactional(readOnly = true)
    public JwtTokenPair login(String userName, String password) {
        User user = userRepository.findByUserName(userName.trim())
                .filter(existingUser -> passwordEncoder.matches(password, existingUser.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos"));

        return jwtTokenService.createTokenPair(user.getUserName());
    }
}
