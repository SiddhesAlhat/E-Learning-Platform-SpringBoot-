package com.elearning.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "virtual_classrooms")
public class VirtualClassroom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime endTime;
    private Integer duration; // in minutes
    private Integer maxParticipants;
    private Integer currentParticipantCount = 0;
    
    @Enumerated(EnumType.STRING)
    private SessionStatus status = SessionStatus.SCHEDULED;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private User instructor;
    
    private String roomId; // WebRTC room identifier
    private String recordingUrl;
    
    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL)
    private List<SessionRecording> recordings = new ArrayList<>();
    
    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL)
    private List<Poll> polls = new ArrayList<>();
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum SessionStatus {
        SCHEDULED, LIVE, ENDED, CANCELLED
    }
}
