package com.elearning.dto;

import com.elearning.model.Course;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private Long categoryId;

    private Course.DifficultyLevel difficultyLevel;

    private BigDecimal price;

    private Integer estimatedDuration;

    private List<String> tags;

    private MultipartFile thumbnail;

    // Getters and setters are handled by Lombok
}
