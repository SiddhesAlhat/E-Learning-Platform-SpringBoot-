package com.elearning.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "learning_style_profiles",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id"}))
public class LearningStyleProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal visualScore;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal auditoryScore;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal readingWritingScore;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal kinestheticScore;
    
    @Enumerated(EnumType.STRING)
    private LearningStyle dominantStyle;
    
    @UpdateTimestamp
    private LocalDateTime lastUpdated;
    
    public enum LearningStyle {
        VISUAL, AUDITORY, READING_WRITING, KINESTHETIC, MULTIMODAL
    }
}
