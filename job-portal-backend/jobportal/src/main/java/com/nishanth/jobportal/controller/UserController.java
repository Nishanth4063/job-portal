package com.nishanth.jobportal.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // MENTOR FIX: Wrapped with ResponseEntity.ok() for consistent 200 OK status
    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users); 
    }

    // MENTOR FIX: Wrapped with ResponseEntity.ok() for consistent 200 OK status
    // SECURITY: a user can view their own profile; anyone else requires ADMIN.
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        assertSelfOrAdmin(id);
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    // Endpoint to delete an existing user record safely
    // SECURITY: a user can delete their own account; anyone else requires ADMIN.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        assertSelfOrAdmin(id);

        // Execute underlying persistence tracking removal step
        userService.deleteUser(id); 
        
        // Return 204 No Content - Clean, performant, and standard
        return ResponseEntity.noContent().build(); 
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
}