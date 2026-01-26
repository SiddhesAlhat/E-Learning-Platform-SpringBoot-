package com.elearning.controller;

import com.elearning.dto.WebRTCSignal;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class WebRTCSignalingController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Handle WebRTC signaling messages (offer, answer, ICE candidates)
     */
    @MessageMapping("/webrtc/signal")
    public void handleSignal(@Payload WebRTCSignal signal) {
        // Forward signal to the specific user or room
        if (signal.getReceiverId() != null) {
            // Send to specific user
            messagingTemplate.convertAndSendToUser(
                    signal.getReceiverId(),
                    "/queue/webrtc",
                    signal
            );
        } else if (signal.getRoomId() != null) {
            // Broadcast to room (all participants)
            messagingTemplate.convertAndSend(
                    "/topic/room/" + signal.getRoomId(),
                    signal
            );
        }
    }

    /**
     * Handle user joining a room
     */
    @MessageMapping("/webrtc/join")
    public void handleJoinRoom(@Payload WebRTCSignal signal) {
        // Notify others in the room
        messagingTemplate.convertAndSend(
                "/topic/room/" + signal.getRoomId() + "/participants",
                Map.of("type", "user-joined", "userId", signal.getSenderId())
        );
    }

    /**
     * Handle user leaving a room
     */
    @MessageMapping("/webrtc/leave")
    public void handleLeaveRoom(@Payload WebRTCSignal signal) {
        // Notify others in the room
        messagingTemplate.convertAndSend(
                "/topic/room/" + signal.getRoomId() + "/participants",
                Map.of("type", "user-left", "userId", signal.getSenderId())
        );
    }

    private static class Map {
        static java.util.Map<String, String> of(String k1, String v1, String k2, String v2) {
            java.util.Map<String, String> map = new java.util.HashMap<>();
            map.put(k1, v1);
            map.put(k2, v2);
            return map;
        }
    }
}
