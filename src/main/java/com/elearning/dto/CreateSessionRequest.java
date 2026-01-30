package com.elearning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSessionRequest {
    @NotBlank(message = "Session title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    @NotNull(message = "Scheduled start time is required")
    private LocalDateTime scheduledStartTime;
    
    @NotNull(message = "Duration is required")
    private Integer duration; // in minutes
    
    private Integer maxParticipants = 50;
}
