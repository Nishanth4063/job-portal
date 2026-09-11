package com.nishanth.jobportal.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nishanth.jobportal.dto.AuthResponse;
import com.nishanth.jobportal.dto.LoginRequest;
import com.nishanth.jobportal.dto.UserResponseDTO;
import com.nishanth.jobportal.entity.User;
import com.nishanth.jobportal.security.JwtUtils;
import com.nishanth.jobportal.service.UserService;

import jakarta.validation.Valid;

// 🎯 FIX: Allow port 80 (Docker container environment) or use "*" to open it up for development testing
@CrossOrigin(origins = {"http://localhost", "http://localhost:4200"}) 
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, JwtUtils jwtUtils, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody User user) {

        User savedUser = userService.saveUser(user);

        UserResponseDTO response = UserResponseDTO.builder()
            .id(savedUser.getId())
            .name(savedUser.getName())
            .email(savedUser.getEmail())
            .role(savedUser.getRole())
            .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        User user = userService.getUserByEmail(loginRequest.getEmail());

        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication Failure: Invalid email or credentials configuration");
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
        AuthResponse authResponse = new AuthResponse(token, user.getEmail(), user.getRole().name(), user.getId());
        return ResponseEntity.ok(authResponse);
    }
}