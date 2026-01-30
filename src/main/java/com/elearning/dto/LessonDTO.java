package com.elearning.dto;

import com.elearning.model.Lesson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonDTO {

    private Long id;
    private String title;
    private String description;
    private Lesson.ContentType contentType;
    private String contentUrl;
    private Integer duration;
    private Integer sequenceOrder;
    private Boolean isPreview;
    private Long moduleId;
    private LocalDateTime createdAt;

    // Progress tracking
    private com.elearning.model.LessonProgress.ProgressStatus userStatus;
    private Integer timeSpent; // in seconds
    private Integer lastPosition; // for video timestamp
    private LocalDateTime completedAt;

    // Additional metadata
    private String fileSize;
    private String mimeType;
    private Boolean hasAssignment;
}
