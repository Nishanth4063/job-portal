package com.nishanth.jobportal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.nishanth.jobportal.entity.Application;
import com.nishanth.jobportal.entity.Job;
import com.nishanth.jobportal.entity.User;
import com.nishanth.jobportal.enums.Role;
import com.nishanth.jobportal.repository.ApplicationRepository;
import com.nishanth.jobportal.repository.JobRepository;
import com.nishanth.jobportal.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private ApplicationService applicationService;

    private User candidate;
    private Job job;
    private MockMultipartFile mockPdf;

    @BeforeEach
    void setUp() {
        candidate = new User();
        candidate.setId(1L);
        candidate.setEmail("candidate@test.com");
        candidate.setRole(Role.CANDIDATE);

        job = new Job();
        job.setId(10L);
        job.setTitle("Software Engineer");

        mockPdf = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "Dummy PDF Content".getBytes()
        );
    }

    @Test
    void applyToJob_WhenCandidateNotFound_ThrowsRuntimeException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                applicationService.applyToJob(1L, 10L, mockPdf));

        assertTrue(exception.getMessage().contains("User not found"));
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void applyToJob_WhenJobNotFound_ThrowsRuntimeException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(10L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                applicationService.applyToJob(1L, 10L, mockPdf));

        assertTrue(exception.getMessage().contains("Job not found"));
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void applyToJob_WhenAlreadyApplied_ThrowsRuntimeException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        when(applicationRepository.existsBySeekerIdAndJobId(1L, 10L)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                applicationService.applyToJob(1L, 10L, mockPdf));

        assertTrue(exception.getMessage().contains("already submitted an application"));
        verify(applicationRepository, never()).save(any(Application.class));
    }
}