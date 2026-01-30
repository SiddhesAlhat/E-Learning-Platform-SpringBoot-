package com.elearning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to start or stop screen sharing in a live session
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreenShareRequest {
    private boolean sharing; // true to start, false to stop
    private String streamId; // WebRTC stream ID for screen share
    private String resolution; // Optional: screen resolution info
    private Integer frameRate; // Optional: frame rate
}
