package com.nishanth.jobportal.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nishanth.jobportal.dto.ApplicationResponseDTO;
import com.nishanth.jobportal.entity.Application;
import com.nishanth.jobportal.entity.User;
import com.nishanth.jobportal.security.CurrentUserProvider;
import com.nishanth.jobportal.service.ApplicationService;
import com.nishanth.jobportal.service.JobService;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final CurrentUserProvider currentUserProvider;
    private final JobService jobService;

    public ApplicationController(ApplicationService applicationService,
                                 CurrentUserProvider currentUserProvider,
                                 JobService jobService) {
        this.applicationService = applicationService;
        this.currentUserProvider = currentUserProvider;
        this.jobService = jobService;
    }

    /**
     * POST /api/applications/apply/{userId}/{jobId}
     * Consumes multipart/form-data to process resume file uploads.
     */
    @PostMapping(value = "/apply/{userId}/{jobId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApplicationResponseDTO> applyToJob(
            @PathVariable Long userId, 
            @PathVariable Long jobId,
            @RequestParam("file") MultipartFile file) {

        currentUserProvider.assertActingAsSelf(userId);

        if (file.isEmpty() || !"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Submission Rejected: Please attach a valid PDF resume document.");
        }

        Application application = applicationService.applyToJob(userId, jobId, file);
        return new ResponseEntity<>(mapToDTO(application), HttpStatus.CREATED);
    }

    /**
     * GET /api/applications/candidate/{userId}
     */
    @GetMapping("/candidate/{userId}")
    public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsByCandidate(@PathVariable Long userId) {
        currentUserProvider.assertActingAsSelf(userId);
        List<ApplicationResponseDTO> response = applicationService.getApplicationsByCandidate(userId)
                .stream()
                .map(this::mapToDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/applications/job/{jobId}
     */
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsByJob(@PathVariable Long jobId) {
        User currentUser = currentUserProvider.getCurrentUser();
        jobService.assertRecruiterOwnsJob(jobId, currentUser.getId());

        List<ApplicationResponseDTO> response = applicationService.getApplicationsByJob(jobId)
                .stream()
                .map(this::mapToDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/applications/recruiter/{recruiterId}
     */
    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsByRecruiter(@PathVariable Long recruiterId) {
        currentUserProvider.assertActingAsSelf(recruiterId);
        List<ApplicationResponseDTO> response = applicationService.getApplicationsByRecruiter(recruiterId)
                .stream()
                .map(this::mapToDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/applications/{applicationId}/status?status=ACCEPTED&employerId=1
     */
    @PutMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationResponseDTO> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestParam String status,
            @RequestParam Long employerId) {

        currentUserProvider.assertActingAsSelf(employerId);
        Application updatedApplication = applicationService.updateApplicationStatus(applicationId, status, employerId);
        return ResponseEntity.ok(mapToDTO(updatedApplication));
    }

    private ApplicationResponseDTO mapToDTO(Application app) {
        return ApplicationResponseDTO.builder()
                .id(app.getId())
                .jobId(app.getJob() != null ? app.getJob().getId() : null)
                .jobTitle(app.getJob() != null ? app.getJob().getTitle() : null)
                .candidateId(app.getSeeker() != null ? app.getSeeker().getId() : null)
                .candidateName(app.getSeeker() != null ? app.getSeeker().getName() : null)
                .candidateEmail(app.getSeeker() != null ? app.getSeeker().getEmail() : null)
                .resumeUrl(app.getResumeUrl())
                .status(app.getStatus())
                .appliedDate(app.getAppliedDate())
                .build();
    }
}