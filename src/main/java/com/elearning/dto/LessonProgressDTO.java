package com.elearning.dto;

import com.elearning.model.LessonProgress;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgressDTO {
    private Long id;
    private Long userId;
    private Long lessonId;
    private String lessonTitle;
    private LessonProgress.ProgressStatus status;
    private Integer timeSpent;
    private Integer lastPosition;
    private java.time.LocalDateTime completedAt;
    private Double completionPercentage;
}
