package com.elearning.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents user progress metrics for adaptive learning
 * Data holder class for tracking learning pace and status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgress {
    private Long userId;
    private Double completionPercentage;
    private Integer coursesCompleted;
    private Integer coursesInProgress;
    private Double averageScore;
    private LocalDateTime lastActivityDate;
    private Double expectedProgress; // What progress should be by now
    private Double actualProgress; // Current progress

    public boolean isBehindSchedule() {
        return actualProgress != null && expectedProgress != null &&
                actualProgress < (expectedProgress * 0.8); // More than 20% behind
    }

    public boolean isAheadOfSchedule() {
        return actualProgress != null && expectedProgress != null &&
                actualProgress > (expectedProgress * 1.2); // More than 20% ahead
    }
}
