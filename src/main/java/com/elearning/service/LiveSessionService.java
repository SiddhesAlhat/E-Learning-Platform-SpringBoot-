package com.elearning.service;

import com.elearning.dto.*;
import com.elearning.model.*;
import com.elearning.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
public class LiveSessionService {

    @Autowired
    private VirtualClassroomRepository virtualClassroomRepository;
    
    @Autowired
    private SessionRecordingRepository sessionRecordingRepository;
    
    @Autowired
    private PollRepository pollRepository;
    
    @Autowired
    private PollVoteRepository pollVoteRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private RecordingService recordingService;

    private static final String SESSION_PARTICIPANTS_KEY = "session:participants:";
    private static final String SESSION_STATUS_KEY = "session:status:";

    public VirtualClassroom createSession(CreateSessionRequest request, User instructor) {
        VirtualClassroom session = VirtualClassroom.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .instructor(instructor)
                .courseId(request.getCourseId())
                .scheduledStartTime(request.getScheduledStartTime())
                .duration(request.getDuration())
                .maxParticipants(request.getMaxParticipants())
                .roomId(UUID.randomUUID().toString())
                .status(VirtualClassroom.SessionStatus.SCHEDULED)
                .build();

        return virtualClassroomRepository.save(session);
    }

    public void addParticipant(String sessionId, String userId) {
        String participantsKey = SESSION_PARTICIPANTS_KEY + sessionId;
        
        // Add to Redis set for fast lookup
        redisTemplate.opsForSet().add(participantsKey, userId);
        
        // Set expiration (24 hours)
        redisTemplate.expire(participantsKey, 24, TimeUnit.HOURS);
        
        // Update participant count in database
        virtualClassroomRepository.incrementParticipantCount(Long.parseLong(sessionId));
        
        // Broadcast participant joined event
        ClassroomEvent event = ClassroomEvent.builder()
                .type("USER_JOINED")
                .userId(userId)
                .timestamp(Instant.now())
                .build();
        
        messagingTemplate.convertAndSend("/topic/classroom/" + sessionId, event);
    }

    public void removeParticipant(String sessionId, String userId) {
        String participantsKey = SESSION_PARTICIPANTS_KEY + sessionId;
        redisTemplate.opsForSet().remove(participantsKey, userId);
        virtualClassroomRepository.decrementParticipantCount(Long.parseLong(sessionId));
        
        // Broadcast participant left event
        ClassroomEvent event = ClassroomEvent.builder()
                .type("USER_LEFT")
                .userId(userId)
                .timestamp(Instant.now())
                .build();
        
        messagingTemplate.convertAndSend("/topic/classroom/" + sessionId, event);
    }

    public Set<String> getActiveParticipants(String sessionId) {
        String key = SESSION_PARTICIPANTS_KEY + sessionId;
        return redisTemplate.opsForSet().members(key)
                .stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    public boolean hasAccess(String userId, String sessionId) {
        VirtualClassroom session = virtualClassroomRepository.findById(Long.parseLong(sessionId))
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        // Instructor always has access
        if (session.getInstructor().getId().toString().equals(userId)) {
            return true;
        }
        
        // Check if user is enrolled in the course
        return enrollmentRepository.existsByUserIdAndCourseId(
            Long.parseLong(userId), 
            session.getCourseId()
        );
    }

    public boolean isInstructor(String sessionId, String userId) {
        VirtualClassroom session = virtualClassroomRepository.findById(Long.parseLong(sessionId))
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        return session.getInstructor().getId().toString().equals(userId);
    }

    public SessionRecording startRecording(String sessionId) {
        VirtualClassroom session = virtualClassroomRepository.findById(Long.parseLong(sessionId))
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        if (!session.getStatus().equals(VirtualClassroom.SessionStatus.LIVE)) {
            throw new RuntimeException("Session must be live to start recording");
        }
        
        return recordingService.startSessionRecording(sessionId);
    }

    public void stopRecording(String sessionId) {
        recordingService.stopSessionRecording(sessionId);
    }

    public void saveChatMessage(String sessionId, ChatMessage message) {
        // Save to MongoDB for history (would implement MongoDB repository)
        // For now, just broadcast the message
        
        ChatMessage broadcastMessage = ChatMessage.builder()
                .id(UUID.randomUUID().toString())
                .sessionId(sessionId)
                .senderId(message.getSenderId())
                .message(message.getMessage())
                .timestamp(Instant.now())
                .build();
        
        messagingTemplate.convertAndSend("/topic/classroom/" + sessionId + "/chat", broadcastMessage);
    }

    public Poll createPoll(String sessionId, CreatePollRequest request) {
        VirtualClassroom session = virtualClassroomRepository.findById(Long.parseLong(sessionId))
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        Poll poll = Poll.builder()
                .id(UUID.randomUUID().toString())
                .classroom(session)
                .question(request.getQuestion())
                .options(convertOptionsToJson(request.getOptions()))
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(request.getDurationSeconds()))
                .build();
        
        poll = pollRepository.save(poll);
        
        // Broadcast poll created event
        PollEvent event = PollEvent.builder()
                .type("POLL_CREATED")
                .poll(poll)
                .build();
        
        messagingTemplate.convertAndSend("/topic/classroom/" + sessionId + "/poll", event);
        
        return poll;
    }

    public void recordVote(String pollId, String userId, String optionId) {
        PollVote vote = PollVote.builder()
                .pollId(pollId)
                .userId(Long.parseLong(userId))
                .optionId(optionId)
                .votedAt(LocalDateTime.now())
                .build();
        
        pollVoteRepository.save(vote);
        
        // Send updated results to instructor
        PollResults results = getPollResults(pollId);
        Poll poll = pollRepository.findById(pollId).orElse(null);
        if (poll != null) {
            String instructorId = poll.getClassroom().getInstructor().getId().toString();
            messagingTemplate.convertAndSendToUser(
                instructorId,
                "/queue/poll/results",
                results
            );
        }
    }

    public PollResults getPollResults(String pollId) {
        List<PollVote> votes = pollVoteRepository.findByPollId(pollId);
        
        Map<String, Long> results = votes.stream()
                .collect(Collectors.groupingBy(
                    PollVote::getOptionId,
                    Collectors.counting()
                ));
        
        return PollResults.builder()
                .pollId(pollId)
                .results(results)
                .totalVotes(votes.size())
                .build();
    }

    public void startSession(String sessionId) {
        VirtualClassroom session = virtualClassroomRepository.findById(Long.parseLong(sessionId))
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        session.setStatus(VirtualClassroom.SessionStatus.LIVE);
        session.setActualStartTime(LocalDateTime.now());
        virtualClassroomRepository.save(session);
        
        // Update Redis status
        redisTemplate.opsForValue().set(SESSION_STATUS_KEY + sessionId, "LIVE", 24, TimeUnit.HOURS);
        
        // Broadcast session started
        ClassroomEvent event = ClassroomEvent.builder()
                .type("SESSION_STARTED")
                .sessionId(sessionId)
                .timestamp(Instant.now())
                .build();
        
        messagingTemplate.convertAndSend("/topic/classroom/" + sessionId, event);
    }

    public void endSession(String sessionId) {
        VirtualClassroom session = virtualClassroomRepository.findById(Long.parseLong(sessionId))
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        session.setStatus(VirtualClassroom.SessionStatus.ENDED);
        session.setEndTime(LocalDateTime.now());
        virtualClassroomRepository.save(session);
        
        // Stop recording if active
        stopRecording(sessionId);
        
        // Clear participants from Redis
        redisTemplate.delete(SESSION_PARTICIPANTS_KEY + sessionId);
        redisTemplate.delete(SESSION_STATUS_KEY + sessionId);
        
        // Broadcast session ended
        ClassroomEvent event = ClassroomEvent.builder()
                .type("SESSION_ENDED")
                .sessionId(sessionId)
                .timestamp(Instant.now())
                .build();
        
        messagingTemplate.convertAndSend("/topic/classroom/" + sessionId, event);
    }

    public List<VirtualClassroom> getUpcomingSessions(Long userId) {
        return virtualClassroomRepository.findUpcomingSessionsForUser(userId);
    }

    public List<VirtualClassroom> getPastSessions(Long userId) {
        return virtualClassroomRepository.findPastSessionsForUser(userId);
    }

    public VirtualClassroom getSessionDetails(String sessionId, Long userId) {
        VirtualClassroom session = virtualClassroomRepository.findById(Long.parseLong(sessionId))
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        if (!hasAccess(userId.toString(), sessionId)) {
            throw new RuntimeException("Access denied");
        }
        
        // Add participant count from Redis
        Set<String> participants = getActiveParticipants(sessionId);
        session.setCurrentParticipantCount(participants.size());
        
        return session;
    }

    private String convertOptionsToJson(List<String> options) {
        // Convert list of options to JSON array format
        // In a real implementation, use a proper JSON library
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < options.size(); i++) {
            if (i > 0) json.append(",");
            json.append("{\"id\":\"").append(i).append("\",\"text\":\"").append(options.get(i)).append("\"}");
        }
        json.append("]");
        return json.toString();
    }
}
