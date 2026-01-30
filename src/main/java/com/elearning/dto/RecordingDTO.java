package com.elearning.dto;

import com.elearning.model.SessionRecording;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordingDTO {
    private Long id;
    private Long sessionId;
    private String recordingUrl;
    private Integer duration;
    private Long fileSize;
    private SessionRecording.RecordingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
