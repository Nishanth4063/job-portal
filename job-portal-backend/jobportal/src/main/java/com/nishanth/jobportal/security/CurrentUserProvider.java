package com.nishanth.jobportal.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.nishanth.jobportal.entity.User;
import com.nishanth.jobportal.exception.UnauthorizedAccessException;
import com.nishanth.jobportal.service.UserService;

/**
 * Resolves the currently authenticated user from the Spring Security context
 * (populated by JwtAuthenticationFilter from the validated JWT) instead of
 * trusting any userId/employerId/recruiterId supplied by the client in the
 * URL or request body.
 */
@Component
public class CurrentUserProvider {

    private final UserService userService;

    public CurrentUserProvider(UserService userService) {
        this.userService = userService;
    }

    /**
     * The user identified by the JWT on the current request.
     */
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByEmail(email);
    }

    /**
     * Throws if the ID a client claimed in the URL/body does not match the
     * ID of whoever is actually authenticated on this request. This is what
     * closes the gap where a logged-in user could act on behalf of another
     * user simply by changing an ID in the request.
     */
    public void assertActingAsSelf(Long claimedUserId) {
        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(claimedUserId)) {
            throw new UnauthorizedAccessException(
                    "Access Denied: You are not authorized to perform this action on behalf of another user.");
        }
    }
}
