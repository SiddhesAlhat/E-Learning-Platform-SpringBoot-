package com.elearning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollResultsDTO {
    private String pollId;
    private Map<String, Long> results;
    private Integer totalVotes;
    private Boolean isActive;
}
