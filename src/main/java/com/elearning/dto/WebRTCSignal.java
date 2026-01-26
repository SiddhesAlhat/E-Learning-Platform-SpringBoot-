package com.elearning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebRTCSignal {
    private String type; // offer, answer, ice-candidate
    private String roomId;
    private String senderId;
    private String receiverId;
    private Object data; // SDP or ICE candidate data
}
