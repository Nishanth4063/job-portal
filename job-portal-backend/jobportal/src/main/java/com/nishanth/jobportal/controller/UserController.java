package com.nishanth.jobportal.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nishanth.jobportal.dto.UserResponseDTO;
import com.nishanth.jobportal.entity.User;
import com.nishanth.jobportal.enums.Role;
import com.nishanth.jobportal.exception.UnauthorizedAccessException;
import com.nishanth.jobportal.security.CurrentUserProvider;
import com.nishanth.jobportal.service.UserService;

@CrossOrigin(origins = "http://localhost:4200") 
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    public UserController(UserService userService, CurrentUserProvider currentUserProvider) {
        this.userService = userService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getUsers() {
        assertAdmin();
        List<UserResponseDTO> users = userService.getAllUsers()
                .stream()
                .map(this::mapToDTO)
                .toList();
        return ResponseEntity.ok(users); 
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long id) {
        assertSelfOrAdmin(id);
        User user = userService.getUserById(id);
        return ResponseEntity.ok(mapToDTO(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        assertSelfOrAdmin(id);
        userService.deleteUser(id); 
        return ResponseEntity.noContent().build(); 
    }

    private UserResponseDTO mapToDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    private void assertSelfOrAdmin(Long targetUserId) {
        User currentUser = currentUserProvider.getCurrentUser();
        boolean isSelf = currentUser.getId().equals(targetUserId);
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isSelf && !isAdmin) {
            throw new UnauthorizedAccessException(
                    "Access Denied: You can only view or modify your own account.");
        }
    }

    private void assertAdmin() {
        User currentUser = currentUserProvider.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccessException("Access Denied: Admin privileges required.");
        }
    }
}