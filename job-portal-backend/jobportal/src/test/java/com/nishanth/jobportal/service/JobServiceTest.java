package com.nishanth.jobportal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nishanth.jobportal.entity.Job;
import com.nishanth.jobportal.entity.User;
import com.nishanth.jobportal.exception.UnauthorizedAccessException;
import com.nishanth.jobportal.repository.JobRepository;

@ExtendWith(MockitoExtension.class)

class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;

    private Job sampleJob;
    private User recruiterUser;
    private User otherRecruiter;

    @BeforeEach
    void setUp() {
        recruiterUser = new User();
        recruiterUser.setId(10L);
        recruiterUser.setEmail("recruiter@techcorp.com");

        otherRecruiter = new User();
        otherRecruiter.setId(99L);
        otherRecruiter.setEmail("other@company.com");

        sampleJob = new Job();
        sampleJob.setId(100L);
        sampleJob.setTitle("Full Stack Engineer");
        sampleJob.setPostedBy(recruiterUser);
    }

    @Test
    void assertRecruiterOwnsJob_WhenUserIsOwner_SucceedsWithoutException() {
        when(jobRepository.findById(100L)).thenReturn(Optional.of(sampleJob));

        assertDoesNotThrow(() -> jobService.assertRecruiterOwnsJob(100L, 10L));
        verify(jobRepository, times(1)).findById(100L);
    }

    @Test
    void assertRecruiterOwnsJob_WhenUserIsNotOwner_ThrowsUnauthorizedAccessException() {
        when(jobRepository.findById(100L)).thenReturn(Optional.of(sampleJob));

        assertThrows(UnauthorizedAccessException.class, 
                () -> jobService.assertRecruiterOwnsJob(100L, 99L));
    }
}