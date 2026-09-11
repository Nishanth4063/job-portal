package com.nishanth.jobportal.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.nishanth.jobportal.entity.User;
import com.nishanth.jobportal.enums.Role;
import com.nishanth.jobportal.exception.DuplicateEmailException; 
import com.nishanth.jobportal.repository.UserRepository;
@ExtendWith(MockitoExtension.class) 
@SuppressWarnings("null") 
class UserServiceTest { 
    @Mock private UserRepository userRepository; 
    @Mock private PasswordEncoder passwordEncoder; 
    @InjectMocks private UserService userService; 
    private User sampleUser; 
    @BeforeEach 
    @SuppressWarnings("unused")
    void setUp() { 
        sampleUser = new User(); 
        sampleUser.setId(1L); 
        sampleUser.setName("Nishanth"); 
        sampleUser.setEmail("nishanth@example.com"); 
        sampleUser.setPassword("rawPassword"); 
        sampleUser.setRole(Role.CANDIDATE);
     } 
    @Test void registerUser_WhenEmailAlreadyExists_ThrowsDuplicateEmailException() { 
        when(userRepository.existsByEmail(sampleUser.getEmail())) .thenReturn(true); 
        DuplicateEmailException exception = assertThrows( DuplicateEmailException.class, () -> userService.registerUser(sampleUser) ); 
        assertNotNull(exception); verify(userRepository, never()).save(any(User.class)); 
    } 
    @Test void registerUser_WhenEmailIsUnique_HashesPasswordAndSavesUser() { 
        when(userRepository.existsByEmail(sampleUser.getEmail())) .thenReturn(false); 
        when(passwordEncoder.encode("rawPassword")) .thenReturn("hashedPassword"); 
        when(userRepository.save(any(User.class))) .thenAnswer(invocation -> invocation.getArgument(0)); 
        User registered = userService.registerUser(sampleUser); 
        assertNotNull(registered); 
        assertEquals("hashedPassword", registered.getPassword()); 
        verify(userRepository, times(1)) 
        .save(sampleUser); 
    }
 
}