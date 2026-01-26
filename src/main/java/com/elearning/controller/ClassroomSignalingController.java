package com.elearning.controller;

import com.elearning.dto.*;
import com.elearning.model.*;
import com.elearning.service.LiveSessionService;
import com.elearning.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@Slf4j
public class ClassroomSignalingController {

    @Autowired
    private LiveSessionService liveSessionService;

    @Autowired
    private UserService userService;

    // When student wants to join classroom
    @MessageMapping("/classroom/{sessionId}/join")
    @SendTo("/topic/classroom/{sessionId}")
    public ClassroomEvent joinClassroom(
            @DestinationVariable String sessionId,
            @Payload JoinRequest request,
            SimpMessageHeaderAccessor headerAccessor) {

        String userId = headerAccessor.getUser().getName();

        // Verify user has access to this session
        if (!liveSessionService.hasAccess(userId, sessionId)) {
            throw new RuntimeException("Access denied to this session");
        }

        // Add user to session
        liveSessionService.addParticipant(sessionId, userId);

        ClassroomEvent event = ClassroomEvent.builder()
                .type("USER_JOINED")
                .userId(userId)
                .username(request.getUsername())
                .timestamp(java.time.Instant.now())
                .build();

        log.info("User {} joined session {}", userId, sessionId);
        return event;
    }

    // WebRTC offer (for peer connection establishment)
    @MessageMapping("/classroom/{sessionId}/offer")
    public void handleOffer(
            @DestinationVariable String sessionId,
            @Payload WebRTCOffer offer,
            SimpMessageHeaderAccessor headerAccessor) {

        String senderId = headerAccessor.getUser().getName();

        // Send offer to specific peer
        headerAccessor.getSessionAttributes().put("targetUserId", offer.getTargetUserId());
        
        // Forward offer to target user via queue
        headerAccessor.convertAndSendToUser(
            offer.getTargetUserId(),
            "/queue/classroom/offer",
            offer
        );

        log.info("Offer sent from {} to {} in session {}", senderId, offer.getTargetUserId(), sessionId);
    }

    // WebRTC answer
    @MessageMapping("/classroom/{sessionId}/answer")
    public void handleAnswer(
            @DestinationVariable String sessionId,
            @Payload WebRTCAnswer answer,
            SimpMessageHeaderAccessor headerAccessor) {

        String senderId = headerAccessor.getUser().getName();

        headerAccessor.convertAndSendToUser(
            answer.getTargetUserId(),
            "/queue/classroom/answer",
            answer
        );

        log.info("Answer sent from {} to {} in session {}", senderId, answer.getTargetUserId(), sessionId);
    }

    // ICE candidates (for NAT traversal)
    @MessageMapping("/classroom/{sessionId}/ice-candidate")
    public void handleIceCandidate(
            @DestinationVariable String sessionId,
            @Payload IceCandidate candidate,
            SimpMessageHeaderAccessor headerAccessor) {

        String senderId = headerAccessor.getUser().getName();

        headerAccessor.convertAndSendToUser(
            candidate.getTargetUserId(),
            "/queue/classroom/ice-candidate",
            candidate
        );
    }

    // Screen sharing
    @MessageMapping("/classroom/{sessionId}/screen-share")
    @SendTo("/topic/classroom/{sessionId}/screen")
    public ScreenShareEvent handleScreenShare(
            @DestinationVariable String sessionId,
            @Payload ScreenShareRequest request,
            SimpMessageHeaderAccessor headerAccessor) {

        String userId = headerAccessor.getUser().getName();

        ScreenShareEvent event = ScreenShareEvent.builder()
                .userId(userId)
                .sharing(request.isSharing())
                .streamId(request.getStreamId())
                .timestamp(java.time.Instant.now())
                .build();

        return event;
    }

    // Chat messages
    @MessageMapping("/classroom/{sessionId}/chat")
    @SendTo("/topic/classroom/{sessionId}/chat")
    public ChatMessage handleChatMessage(
            @DestinationVariable String sessionId,
            @Payload ChatMessage message,
            SimpMessageHeaderAccessor headerAccessor) {

        String userId = headerAccessor.getUser().getName();
        message.setSenderId(userId);
        message.setSessionId(sessionId);
        message.setTimestamp(java.time.Instant.now());

        // Save chat message to database
        liveSessionService.saveChatMessage(sessionId, message);

        return message;
    }

    // Raise hand feature
    @MessageMapping("/classroom/{sessionId}/raise-hand")
    @SendTo("/topic/classroom/{sessionId}/hands")
    public HandRaisedEvent handleHandRaise(
            @DestinationVariable String sessionId,
            SimpMessageHeaderAccessor headerAccessor) {

        String userId = headerAccessor.getUser().getName();

        HandRaisedEvent event = HandRaisedEvent.builder()
                .userId(userId)
                .timestamp(java.time.Instant.now())
                .build();

        return event;
    }

    // Whiteboard collaboration
    @MessageMapping("/classroom/{sessionId}/whiteboard")
    @SendTo("/topic/classroom/{sessionId}/whiteboard")
    public WhiteboardAction handleWhiteboardAction(
            @DestinationVariable String sessionId,
            @Payload WhiteboardAction action,
            SimpMessageHeaderAccessor headerAccessor) {

        String userId = headerAccessor.getUser().getName();
        action.setUserId(userId);

        // Save whiteboard state
        // liveSessionService.saveWhiteboardAction(sessionId, action);

        return action;
    }

    // Polls during session
    @MessageMapping("/classroom/{sessionId}/poll")
    @SendTo("/topic/classroom/{sessionId}/poll")
    public PollEvent createPoll(
            @DestinationVariable String sessionId,
            @Payload CreatePollRequest request,
            SimpMessageHeaderAccessor headerAccessor) {

        String instructorId = headerAccessor.getUser().getName();

        // Only instructor can create polls
        if (!liveSessionService.isInstructor(sessionId, instructorId)) {
            throw new RuntimeException("Only instructor can create polls");
        }

        Poll poll = liveSessionService.createPoll(sessionId, request);

        PollEvent event = PollEvent.builder()
                .type("POLL_CREATED")
                .poll(poll)
                .build();

        return event;
    }

    @MessageMapping("/classroom/{sessionId}/poll/{pollId}/vote")
    public void votePoll(
            @DestinationVariable String sessionId,
            @DestinationVariable String pollId,
            @Payload VoteRequest vote,
            SimpMessageHeaderAccessor headerAccessor) {

        String userId = headerAccessor.getUser().getName();

        liveSessionService.recordVote(pollId, userId, vote.getOptionId());
    }

    // Recording controls
    @MessageMapping("/classroom/{sessionId}/recording/start")
    @SendTo("/topic/classroom/{sessionId}/recording")
    public RecordingEvent startRecording(
            @DestinationVariable String sessionId,
            SimpMessageHeaderAccessor headerAccessor) {

        String instructorId = headerAccessor.getUser().getName();

        if (!liveSessionService.isInstructor(sessionId, instructorId)) {
            throw new RuntimeException("Only instructor can start recording");
        }

        SessionRecording recording = liveSessionService.startRecording(sessionId);

        RecordingEvent event = RecordingEvent.builder()
                .status("RECORDING")
                .recordingId(recording.getId().toString())
                .timestamp(java.time.Instant.now())
                .build();

        return event;
    }

    @MessageMapping("/classroom/{sessionId}/recording/stop")
    @SendTo("/topic/classroom/{sessionId}/recording")
    public RecordingEvent stopRecording(
            @DestinationVariable String sessionId,
            SimpMessageHeaderAccessor headerAccessor) {

        String instructorId = headerAccessor.getUser().getName();

        if (!liveSessionService.isInstructor(sessionId, instructorId)) {
            throw new RuntimeException("Only instructor can stop recording");
        }

        liveSessionService.stopRecording(sessionId);

        RecordingEvent event = RecordingEvent.builder()
                .status("STOPPED")
                .timestamp(java.time.Instant.now())
                .build();

        return event;
    }

    // Handle disconnection
    @MessageMapping("/classroom/{sessionId}/leave")
    public void leaveClassroom(
            @DestinationVariable String sessionId,
            SimpMessageHeaderAccessor headerAccessor) {

        String userId = headerAccessor.getUser().getName();
        liveSessionService.removeParticipant(sessionId, userId);

        ClassroomEvent event = ClassroomEvent.builder()
                .type("USER_LEFT")
                .userId(userId)
                .timestamp(java.time.Instant.now())
                .build();

        headerAccessor.convertAndSend("/topic/classroom/" + sessionId, event);
    }

    // Subscribe to session status updates
    @SubscribeMapping("/topic/classroom/{sessionId}")
    public ClassroomEvent getSessionStatus(@DestinationVariable String sessionId) {
        return ClassroomEvent.builder()
                .type("SESSION_STATUS")
                .sessionId(sessionId)
                .timestamp(java.time.Instant.now())
                .build();
    }
}
