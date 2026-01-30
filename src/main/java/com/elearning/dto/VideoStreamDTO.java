package com.elearning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoStreamDTO {
    private Long lessonId;
    private String streamUrl;
    private String token;
    private String resolution;
    private String format; // HLS, DASH, MP4
    private Boolean isLive;
}
