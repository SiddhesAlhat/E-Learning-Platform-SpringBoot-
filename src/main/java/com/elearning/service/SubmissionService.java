package com.elearning.service;

import com.elearning.model.*;
import com.elearning.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AutoGradingService autoGradingService;

    @Autowired
    private PlagiarismDetectionService plagiarismService;

    @Autowired
    private GamificationService gamificationService;

    /**
     * Submit an assignment
     */
    public Submission submitAssignment(Long assignmentId, Long studentId, String content, String fileUrl) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Check if already submitted
        submissionRepository.findByAssignmentAndStudent(assignment, student)
                .ifPresent(existing -> {
                    throw new RuntimeException("Assignment already submitted");
                });

        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setContent(content);
        submission.setFileUrl(fileUrl);
        submission.setSubmittedAt(LocalDateTime.now());

        // Check for plagiarism against previous submissions
        double plagiarismScore = checkPlagiarism(submission, assignment);
        submission.setPlagiarismScore(plagiarismScore);

        // Auto-grade if enabled
        if (assignment.isAutoGrade()) {
            gradeSubmission(submission, assignment);
        }

        submission = submissionRepository.save(submission);

        // Award points for submission
        gamificationService.awardPoints(student, "ASSIGNMENT_SUBMIT");

        return submission;
    }

    /**
     * Grade a submission
     */
    public Submission gradeSubmission(Submission submission, Assignment assignment) {
        Map<String, Object> gradingResult = autoGradingService.gradeSubmission(submission, assignment);

        int score = (int) gradingResult.get("score");
        String feedback = (String) gradingResult.get("feedback");

        submission.setScore(score);
        submission.setFeedback(feedback);
        submission.setGrade(autoGradingService.calculateGrade(score, assignment.getMaxScore()));
        submission.setGraded(true);

        // Award bonus points for passing
        if (score >= assignment.getMaxScore() * 0.7) { // 70% pass rate
            gamificationService.awardPoints(submission.getStudent(), "QUIZ_PASS");
        }

        return submissionRepository.save(submission);
    }

    /**
     * Check submission for plagiarism
     */
    private double checkPlagiarism(Submission newSubmission, Assignment assignment) {
        // Get all previous submissions for this assignment
        var previousSubmissions = submissionRepository.findByAssignment(assignment);

        double maxSimilarity = 0.0;

        for (Submission previous : previousSubmissions) {
            double similarity;

            if ("CODE".equals(assignment.getType())) {
                similarity = plagiarismService.calculateCodePlagiarismScore(
                        newSubmission.getContent(), 
                        previous.getContent()
                );
            } else {
                similarity = plagiarismService.calculatePlagiarismScore(
                        newSubmission.getContent(), 
                        previous.getContent()
                );
            }

            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
            }
        }

        return maxSimilarity;
    }
}
