package com.elearning.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "polls")
public class Poll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id; // Using String ID for compatibility with WebRTC session IDs
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private VirtualClassroom classroom;
    
    @Column(columnDefinition = "TEXT")
    private String question;
    
    @Column(columnDefinition = "JSONB")
    private String options; // JSON array of {id, text}
    
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private LocalDateTime createdAt;
    
    private LocalDateTime expiresAt;
    
    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL)
    private List<PollVote> votes;
}
