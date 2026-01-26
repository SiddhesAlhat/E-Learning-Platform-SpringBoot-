package com.elearning.service;

import com.elearning.model.Assignment;
import com.elearning.model.Submission;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AutoGradingService {

    /**
     * Auto-grade a submission based on assignment type
     * Returns a map with score and feedback
     */
    public Map<String, Object> gradeSubmission(Submission submission, Assignment assignment) {
        Map<String, Object> result = new HashMap<>();

        String type = assignment.getType();
        if (type == null) type = "QUIZ";

        switch (type) {
            case "QUIZ":
                return gradeQuizSubmission(submission, assignment);
            case "CODE":
                return gradeCodeSubmission(submission, assignment);
            default:
                result.put("score", 0);
                result.put("feedback", "Manual grading required");
                return result;
        }
    }

    private Map<String, Object> gradeQuizSubmission(Submission submission, Assignment assignment) {
        Map<String, Object> result = new HashMap<>();
        
        String studentAnswer = submission.getContent().trim().toLowerCase();
        String correctAnswer = assignment.getCorrectAnswer().trim().toLowerCase();

        // Check for exact match
        if (studentAnswer.equals(correctAnswer)) {
            result.put("score", assignment.getMaxScore());
            result.put("feedback", "Correct answer!");
        } else {
            // Partial credit for keyword matching
            int keywordMatches = countKeywordMatches(studentAnswer, correctAnswer);
            String[] correctKeywords = correctAnswer.split("\\s+");
            
            double partialScore = ((double) keywordMatches / correctKeywords.length) * assignment.getMaxScore();
            result.put("score", (int) Math.round(partialScore));
            
            if (partialScore > 0) {
                result.put("feedback", "Partial credit awarded. Answer contains some correct elements.");
            } else {
                result.put("feedback", "Incorrect answer. Expected: " + assignment.getCorrectAnswer());
            }
        }

        return result;
    }

    private int countKeywordMatches(String studentAnswer, String correctAnswer) {
        String[] keywords = correctAnswer.split("\\s+");
        int matches = 0;
        
        for (String keyword : keywords) {
            if (studentAnswer.contains(keyword)) {
                matches++;
            }
        }
        
        return matches;
    }

    private Map<String, Object> gradeCodeSubmission(Submission submission, Assignment assignment) {
        Map<String, Object> result = new HashMap<>();
        String studentCode = submission.getContent();

        // Simple test case evaluation (in production, you'd execute the code safely)
        String testCases = assignment.getTestCases();
        if (testCases == null || testCases.isEmpty()) {
            result.put("score", 0);
            result.put("feedback", "No test cases defined for this assignment");
            return result;
        }

        // Parse test cases (format: input|expected_output)
        String[] tests = testCases.split(";");
        int passedTests = 0;

        for (String test : tests) {
            // This is a simplified version - in production you'd use a code execution sandbox
            // For now, just check if certain patterns exist in the code
            String[] parts = test.split("\\|");
            if (parts.length == 2) {
                String expectedPattern = parts[1].trim();
                if (studentCode.contains(expectedPattern)) {
                    passedTests++;
                }
            }
        }

        double scorePercentage = (double) passedTests / tests.length;
        int score = (int) Math.round(scorePercentage * assignment.getMaxScore());

        result.put("score", score);
        result.put("feedback", String.format("Passed %d out of %d test cases", passedTests, tests.length));

        return result;
    }

    /**
     * Calculate grade letter based on score percentage
     */
    public String calculateGrade(int score, int maxScore) {
        double percentage = ((double) score / maxScore) * 100;

        if (percentage >= 90) return "A";
        else if (percentage >= 80) return "B";
        else if (percentage >= 70) return "C";
        else if (percentage >= 60) return "D";
        else return "F";
    }
}
