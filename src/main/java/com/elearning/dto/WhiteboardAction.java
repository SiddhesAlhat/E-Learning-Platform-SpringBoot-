package com.elearning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhiteboardAction {
    private String userId;
    private String type; // DRAW, ERASE, TEXT, etc.
    private Object data; // Drawing data
    private Integer x;
    private Integer y;
}
