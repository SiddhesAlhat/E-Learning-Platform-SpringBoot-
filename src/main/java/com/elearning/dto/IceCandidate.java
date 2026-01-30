package com.elearning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IceCandidate {
    private String targetUserId;
    private String candidate;
    private String sdpMid;
    private Integer sdpMLineIndex;
}
