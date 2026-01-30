package com.elearning.dto;

import com.elearning.model.Lesson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLessonRequest {
    
    @NotBlank(message = "Lesson title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Content type is required")
    private Lesson.ContentType contentType;
    
    private Integer duration;
    
    private Boolean isPreview = false;
    
    private MultipartFile contentFile;
}
