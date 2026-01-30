package com.elearning.dto;

import com.elearning.model.VirtualClassroom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualClassroomDTO {
    private Long id;
    private String title;
    private String description;
    private String roomId;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime endTime;
    private Integer duration;
    private Integer maxParticipants;
    private Integer currentParticipantCount;
    private VirtualClassroom.SessionStatus status;
    private String recordingUrl;
    private Long instructorId;
    private String instructorName;
    private Long courseId;
    private String courseTitle;
}
