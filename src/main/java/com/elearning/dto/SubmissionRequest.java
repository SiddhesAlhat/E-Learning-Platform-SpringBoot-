package com.elearning.dto;

import lombok.Data;

@Data
public class SubmissionRequest {
    private Long assignmentId;
    private String content;
    private String fileUrl;
}
