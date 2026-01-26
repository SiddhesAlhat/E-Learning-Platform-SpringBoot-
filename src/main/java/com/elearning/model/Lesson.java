package com.elearning.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "lessons")
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    private ContentType contentType;
    
    private String contentUrl;
    private Integer duration; // in minutes
    private Integer sequenceOrder;
    private Boolean isPreview = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private Module module;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL)
    private List<LessonContent> content = new ArrayList<>();
    
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL)
    private List<LessonProgress> progress = new ArrayList<>();
    
    // For quizzes
    @OneToOne(mappedBy = "lesson", cascade = CascadeType.ALL)
    private Assignment assignment;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    public enum ContentType {
        VIDEO, TEXT, DOCUMENT, QUIZ, INTERACTIVE, LIVE_SESSION
    }
}
