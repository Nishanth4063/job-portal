package com.nishanth.jobportal.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nishanth.jobportal.dto.JobResponseDTO;
import com.nishanth.jobportal.entity.Job;
import com.nishanth.jobportal.entity.User;
import com.nishanth.jobportal.enums.Role;
import com.nishanth.jobportal.exception.UnauthorizedAccessException;
import com.nishanth.jobportal.repository.JobRepository;
import com.nishanth.jobportal.repository.UserRepository;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    // Constructor Injection (Industry Standard over @Autowired)
    public JobService(JobRepository jobRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    // --- CONVERSION METHOD (ENTITY -> DTO) ---
    private JobResponseDTO mapToDTO(Job job) {
        JobResponseDTO dto = new JobResponseDTO();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setDescription(job.getDescription());
        dto.setLocation(job.getLocation());
        
        // MENTOR FIX: Safely convert Double from Entity to BigDecimal for DTO mapping
        if (job.getSalary() != null) {
            dto.setSalary(BigDecimal.valueOf(job.getSalary()));
        }
        
        dto.setPostedDate(job.getPostedDate());
        
        if (job.getPostedBy() != null) {
            dto.setPostedByName(job.getPostedBy().getName());
        }
        return dto;
    }

    // Fetch all jobs mapped to secure DTO data streams
    public List<JobResponseDTO> fetchAllJobs() {
        return jobRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Multi-conditional filtering logic for job listings
    public List<JobResponseDTO> searchJobs(String title, String location) {
        List<Job> jobs;
        if (title != null && !title.isEmpty() && location != null && !location.isEmpty()) {
            jobs = jobRepository.findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCase(title, location);
        } else if (title != null && !title.isEmpty()) {
            jobs = jobRepository.findByTitleContainingIgnoreCase(title);
        } else if (location != null && !location.isEmpty()) {
            jobs = jobRepository.findByLocationContainingIgnoreCase(location);
        } else {
            jobs = jobRepository.findAll();
        }

        return jobs.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Secure persistence method returning a standardized data carrier signature
    public JobResponseDTO saveJob(Job job, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // RBAC Enforcement Check
        if (user.getRole() != Role.RECRUITER) {
            throw new RuntimeException("Access Denied: Only Recruiters can post jobs.");
        }

        // Establish core relational metadata boundaries
        job.setPostedBy(user);
        
        if (job.getPostedDate() == null) {
            job.setPostedDate(LocalDateTime.now());
        }
        
        job.setPostedByName(user.getName());

        // Persist entity data row to SQL Server
        Job savedJob = jobRepository.save(job);

        // Convert and return outbound DTO model envelope
        return this.mapToDTO(savedJob);
    }

    /**
     * 🎯 NEW METHOD: Fetches all jobs posted exclusively by a specific recruiter profile ID
     * maps them cleanly into a standard outbound stream of JobResponseDTO records.
     */
    public List<JobResponseDTO> getJobsByRecruiter(Long recruiterId) {
        if (recruiterId == null) {
            throw new IllegalArgumentException("Recruiter ID must not be null");
        }
        
        return jobRepository.findByPostedById(recruiterId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * SECURITY: Confirms that the given job was actually posted by the given
     * recruiter, before letting that recruiter see who applied to it.
     * Without this, any authenticated recruiter could view applications for
     * a job they never posted, just by guessing/incrementing a jobId.
     */
    public void assertRecruiterOwnsJob(Long jobId, Long recruiterId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Job not found with ID: " + jobId));

        if (job.getPostedBy() == null || !job.getPostedBy().getId().equals(recruiterId)) {
            throw new UnauthorizedAccessException(
                    "Access Denied: You are not authorized to view applications for a job you did not post.");
        }
    }
}