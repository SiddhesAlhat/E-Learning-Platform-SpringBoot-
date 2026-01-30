package com.elearning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePollRequest {
    @NotBlank(message = "Poll question is required")
    private String question;
    
    @NotEmpty(message = "Poll options are required")
    private List<String> options;
    
    private Integer durationSeconds = 60; // Default 1 minute
}
