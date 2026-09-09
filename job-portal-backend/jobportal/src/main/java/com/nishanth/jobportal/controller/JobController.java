package com.nishanth.jobportal.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nishanth.jobportal.dto.JobResponseDTO;
import com.nishanth.jobportal.entity.Job;
import com.nishanth.jobportal.security.CurrentUserProvider;
import com.nishanth.jobportal.service.JobService;

@CrossOrigin(origins = "http://localhost:4200") 
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;
    private final CurrentUserProvider currentUserProvider;

    // Constructor Injection (Industry Standard)
    public JobController(JobService jobService, CurrentUserProvider currentUserProvider) {
        this.jobService = jobService;
        this.currentUserProvider = currentUserProvider;
    }

    // POST /api/jobs/create/{userId}
    // SECURITY: userId in the URL is only accepted if it matches the JWT-authenticated user.
    @PostMapping("/create/{userId}")
    public ResponseEntity<JobResponseDTO> postJob(@RequestBody Job job, @PathVariable Long userId) {
        currentUserProvider.assertActingAsSelf(userId);
        JobResponseDTO savedJobDto = jobService.saveJob(job, userId);
        return new ResponseEntity<>(savedJobDto, HttpStatus.CREATED); // 201 Created
    }

    // GET /api/jobs/all
    @GetMapping("/all")
    public ResponseEntity<List<JobResponseDTO>> getAllJobs() { 
        List<JobResponseDTO> jobs = jobService.fetchAllJobs();
        return ResponseEntity.ok(jobs); // 200 OK
    }

    // GET /api/jobs/search?title=Java&location=Bengaluru
    @GetMapping("/search")
    public ResponseEntity<List<JobResponseDTO>> searchJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location) {
        
        List<JobResponseDTO> results = jobService.searchJobs(title, location); 
        return ResponseEntity.ok(results); // 200 OK
    }

    // 🎯 NEW ENDPOINT: GET /api/jobs/recruiter/{recruiterId}
    // SECURITY: a recruiter can only list their own posted jobs, not another recruiter's.
    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<JobResponseDTO>> getJobsByRecruiter(@PathVariable Long recruiterId) {
        currentUserProvider.assertActingAsSelf(recruiterId);
        List<JobResponseDTO> recruiterJobs = jobService.getJobsByRecruiter(recruiterId);
        return ResponseEntity.ok(recruiterJobs); // 200 OK
    }
}