package com.elearning.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "session_recordings")
public class SessionRecording {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private VirtualClassroom classroom;
    
    private String storageUrl;
    private Integer duration; // in seconds
    private Long fileSize;
    
    @Enumerated(EnumType.STRING)
    private RecordingStatus status = RecordingStatus.PROCESSING;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    public enum RecordingStatus {
        PROCESSING, READY, FAILED
    }
}
