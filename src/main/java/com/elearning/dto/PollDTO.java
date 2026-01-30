package com.elearning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollDTO {
    private String id;
    private String question;
    private java.util.List<String> options;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Integer totalVotes;
    private Boolean isActive;
}
