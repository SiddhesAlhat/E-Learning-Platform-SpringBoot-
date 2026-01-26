package com.elearning.controller;

import com.elearning.dto.SubmissionRequest;
import com.elearning.model.Submission;
import com.elearning.model.User;
import com.elearning.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private com.elearning.repository.UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Submission> submitAssignment(
            @RequestBody SubmissionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User student = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Submission submission = submissionService.submitAssignment(
                request.getAssignmentId(),
                student.getId(),
                request.getContent(),
                request.getFileUrl()
        );

        return ResponseEntity.ok(submission);
    }
}
