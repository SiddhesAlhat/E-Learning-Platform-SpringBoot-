package com.elearning.controller;

import com.elearning.dto.*;
import com.elearning.model.*;
import com.elearning.service.LiveSessionService;
import com.elearning.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/virtual-classroom")
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class VirtualClassroomController {

    @Autowired
    private LiveSessionService liveSessionService;

    @Autowired
    private UserService userService;

    // Create new virtual classroom session
    @PostMapping("/sessions")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<VirtualClassroomDTO> createSession(
            @Valid @RequestBody CreateSessionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User instructor = userService.findByUsername(userDetails.getUsername());
        VirtualClassroom session = liveSessionService.createSession(request, instructor);
        
        VirtualClassroomDTO dto = new VirtualClassroomDTO();
        dto.setId(session.getId());
        dto.setTitle(session.getTitle());
        dto.setDescription(session.getDescription());
        dto.setRoomId(session.getRoomId());
        dto.setScheduledStartTime(session.getScheduledStartTime());
        dto.setDuration(session.getDuration());
        dto.setMaxParticipants(session.getMaxParticipants());
        dto.setStatus(session.getStatus());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // Get session details
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<VirtualClassroomDTO> getSession(
            @PathVariable String sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userService.findByUsername(userDetails.getUsername());
        VirtualClassroom session = liveSessionService.getSessionDetails(sessionId, user.getId());
        
        VirtualClassroomDTO dto = new VirtualClassroomDTO();
        dto.setId(session.getId());
        dto.setTitle(session.getTitle());
        dto.setDescription(session.getDescription());
        dto.setRoomId(session.getRoomId());
        dto.setScheduledStartTime(session.getScheduledStartTime());
        dto.setActualStartTime(session.getActualStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setDuration(session.getDuration());
        dto.setMaxParticipants(session.getMaxParticipants());
        dto.setCurrentParticipantCount(session.getCurrentParticipantCount());
        dto.setStatus(session.getStatus());
        dto.setRecordingUrl(session.getRecordingUrl());
        
        return ResponseEntity.ok(dto);
    }

    // Start session
    @PostMapping("/sessions/{sessionId}/start")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<Void> startSession(
            @PathVariable String sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User instructor = userService.findByUsername(userDetails.getUsername());
        
        // Verify instructor owns this session
        if (!liveSessionService.isInstructor(sessionId, instructor.getId().toString())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        liveSessionService.startSession(sessionId);
        return ResponseEntity.ok().build();
    }

    // End session
    @PostMapping("/sessions/{sessionId}/end")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<Void> endSession(
            @PathVariable String sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User instructor = userService.findByUsername(userDetails.getUsername());
        
        if (!liveSessionService.isInstructor(sessionId, instructor.getId().toString())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        liveSessionService.endSession(sessionId);
        return ResponseEntity.ok().build();
    }

    // Join session
    @PostMapping("/sessions/{sessionId}/join")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> joinSession(
            @PathVariable String sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userService.findByUsername(userDetails.getUsername());
        
        if (!liveSessionService.hasAccess(user.getId().toString(), sessionId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        liveSessionService.addParticipant(sessionId, user.getId().toString());
        return ResponseEntity.ok().build();
    }

    // Leave session
    @PostMapping("/sessions/{sessionId}/leave")
    public ResponseEntity<Void> leaveSession(
            @PathVariable String sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userService.findByUsername(userDetails.getUsername());
        liveSessionService.removeParticipant(sessionId, user.getId().toString());
        return ResponseEntity.ok().build();
    }

    // Get upcoming sessions for user
    @GetMapping("/sessions/upcoming")
    public ResponseEntity<List<VirtualClassroomDTO>> getUpcomingSessions(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userService.findByUsername(userDetails.getUsername());
        List<VirtualClassroom> sessions = liveSessionService.getUpcomingSessions(user.getId());
        
        List<VirtualClassroomDTO> dtos = sessions.stream()
                .map(session -> {
                    VirtualClassroomDTO dto = new VirtualClassroomDTO();
                    dto.setId(session.getId());
                    dto.setTitle(session.getTitle());
                    dto.setRoomId(session.getRoomId());
                    dto.setScheduledStartTime(session.getScheduledStartTime());
                    dto.setDuration(session.getDuration());
                    dto.setStatus(session.getStatus());
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    // Get past sessions for user
    @GetMapping("/sessions/past")
    public ResponseEntity<List<VirtualClassroomDTO>> getPastSessions(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userService.findByUsername(userDetails.getUsername());
        List<VirtualClassroom> sessions = liveSessionService.getPastSessions(user.getId());
        
        List<VirtualClassroomDTO> dtos = sessions.stream()
                .map(session -> {
                    VirtualClassroomDTO dto = new VirtualClassroomDTO();
                    dto.setId(session.getId());
                    dto.setTitle(session.getTitle());
                    dto.setRoomId(session.getRoomId());
                    dto.setScheduledStartTime(session.getScheduledStartTime());
                    dto.setEndTime(session.getEndTime());
                    dto.setDuration(session.getDuration());
                    dto.setStatus(session.getStatus());
                    dto.setRecordingUrl(session.getRecordingUrl());
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    // Legacy endpoints for backward compatibility
    @GetMapping("/legacy")
    public ResponseEntity<List<VirtualClassroom>> getActiveClassrooms() {
        // Legacy endpoint - would need to be updated
        return ResponseEntity.ok(List.of());
    }

    @PostMapping("/legacy")
    public ResponseEntity<VirtualClassroom> createClassroom(
            @RequestBody VirtualClassroom classroom,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Legacy endpoint - would need to be updated
        return ResponseEntity.ok(classroom);
    }

    @GetMapping("/legacy/{id}")
    public ResponseEntity<VirtualClassroom> getClassroom(@PathVariable Long id) {
        // Legacy endpoint - would need to be updated
        return ResponseEntity.ok(new VirtualClassroom());
    }

    @PutMapping("/legacy/{id}/end")
    public ResponseEntity<?> endClassroom(@PathVariable Long id) {
        // Legacy endpoint - would need to be updated
        return ResponseEntity.ok("Classroom ended successfully");
    }
}
