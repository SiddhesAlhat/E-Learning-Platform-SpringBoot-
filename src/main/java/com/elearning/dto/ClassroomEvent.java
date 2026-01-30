package com.elearning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomEvent {
    private String type;
    private String userId;
    private String username;
    private String sessionId;
    private Instant timestamp;
}
