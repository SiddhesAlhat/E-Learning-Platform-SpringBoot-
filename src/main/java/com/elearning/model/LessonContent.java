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
@Table(name = "lesson_content")
public class LessonContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;
    
    @Enumerated(EnumType.STRING)
    private ContentType contentType;
    
    private String storageKey; // S3/CDN key
    private Long fileSize;
    private String mimeType;
    private String transcriptionUrl; // for videos
    private String subtitleUrl;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    public enum ContentType {
        VIDEO, AUDIO, DOCUMENT, IMAGE, INTERACTIVE_ELEMENT
    }
}
