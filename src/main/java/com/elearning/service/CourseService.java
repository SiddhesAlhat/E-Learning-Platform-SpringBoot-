package com.elearning.service;

import com.elearning.dto.CourseDTO;
import com.elearning.dto.CreateCourseRequest;
import com.elearning.dto.CourseDetailDTO;
import com.elearning.dto.ModuleDTO;
import com.elearning.dto.CreateModuleRequest;
import com.elearning.dto.LessonDTO;
import com.elearning.dto.CreateLessonRequest;
import com.elearning.model.Category;
import com.elearning.model.Course;
import com.elearning.model.Lesson;
import com.elearning.model.LessonProgress;
import com.elearning.model.Module;
import com.elearning.model.User;
import com.elearning.repository.CategoryRepository;
import com.elearning.repository.CourseRepository;
import com.elearning.repository.EnrollmentRepository;
import com.elearning.repository.LessonProgressRepository;
import com.elearning.repository.LessonRepository;
import com.elearning.repository.ModuleRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private MediaStorageService mediaStorageService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private ModelMapper modelMapper;

    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .collect(Collectors.toList());
    }

    public Page<CourseDTO> getPublishedCourses(Pageable pageable) {
        return courseRepository.findByIsPublishedTrue(pageable)
                .map(course -> modelMapper.map(course, CourseDTO.class));
    }

    @Cacheable(value = "courses", key = "#courseId")
    public CourseDetailDTO getCourseWithProgress(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Calculate user progress
        List<LessonProgress> userProgress = lessonProgressRepository
                .findByUserIdAndCourseId(userId, courseId);

        CourseDetailDTO dto = modelMapper.map(course, CourseDetailDTO.class);
        dto.setUserProgress(calculateProgress(userProgress).doubleValue());
        dto.setEnrolled(enrollmentRepository.existsByUserIdAndCourseId(userId, courseId));

        return dto;
    }

    public CourseDTO createCourse(CreateCourseRequest request, User instructor) throws IOException {
        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .instructor(instructor)
                .difficultyLevel(request.getDifficultyLevel())
                .price(request.getPrice())
                .estimatedDuration(request.getEstimatedDuration())
                .tags(request.getTags())
                .build();

        // Set category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            course.setCategory(category);
        }

        // Handle thumbnail upload
        if (request.getThumbnail() != null && !request.getThumbnail().isEmpty()) {
            String thumbnailUrl = mediaStorageService.uploadImage(
                    request.getThumbnail(),
                    "course-thumbnails");
            course.setThumbnailUrl(thumbnailUrl);
        }

        Course savedCourse = courseRepository.save(course);

        // Index in search service
        searchService.indexCourse(savedCourse);

        return modelMapper.map(savedCourse, CourseDTO.class);
    }

    @CacheEvict(value = "courses", key = "#courseId")
    public ModuleDTO addModuleToCourse(Long courseId, CreateModuleRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Module module = Module.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .sequenceOrder(course.getModules().size() + 1)
                .course(course)
                .build();

        Module savedModule = moduleRepository.save(module);
        course.addModule(savedModule);
        courseRepository.save(course);

        return modelMapper.map(savedModule, ModuleDTO.class);
    }

    @CacheEvict(value = "courses", key = "#moduleId")
    public LessonDTO addLessonToModule(Long moduleId, CreateLessonRequest request) throws IOException {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        Lesson lesson = Lesson.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .contentType(request.getContentType())
                .duration(request.getDuration())
                .sequenceOrder(module.getLessons().size() + 1)
                .isPreview(request.getIsPreview())
                .module(module)
                .build();

        // Handle file upload for video/document lessons
        if (request.getContentFile() != null && !request.getContentFile().isEmpty()) {
            String contentUrl;
            switch (request.getContentType()) {
                case VIDEO:
                    contentUrl = mediaStorageService.uploadVideo(request.getContentFile(), "lesson-videos");
                    break;
                case DOCUMENT:
                    contentUrl = mediaStorageService.uploadDocument(request.getContentFile(), "lesson-documents");
                    break;
                default:
                    contentUrl = null;
            }
            lesson.setContentUrl(contentUrl);
        }

        Lesson savedLesson = lessonRepository.save(lesson);
        module.addLesson(savedLesson);
        moduleRepository.save(module);

        return modelMapper.map(savedLesson, LessonDTO.class);
    }

    public CourseDTO updateCourse(Long courseId, CreateCourseRequest request, User instructor) throws IOException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if user is the instructor
        if (!course.getInstructor().getId().equals(instructor.getId())) {
            throw new RuntimeException("Only the course instructor can update this course");
        }

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setDifficultyLevel(request.getDifficultyLevel());
        course.setPrice(request.getPrice());
        course.setEstimatedDuration(request.getEstimatedDuration());
        course.setTags(request.getTags());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            course.setCategory(category);
        }

        // Handle thumbnail update
        if (request.getThumbnail() != null && !request.getThumbnail().isEmpty()) {
            String thumbnailUrl = mediaStorageService.uploadImage(
                    request.getThumbnail(),
                    "course-thumbnails");
            course.setThumbnailUrl(thumbnailUrl);
        }

        Course updatedCourse = courseRepository.save(course);

        // Update search index
        searchService.indexCourse(updatedCourse);

        return modelMapper.map(updatedCourse, CourseDTO.class);
    }

    public void deleteCourse(Long courseId, User instructor) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getInstructor().getId().equals(instructor.getId())) {
            throw new RuntimeException("Only the course instructor can delete this course");
        }

        courseRepository.delete(course);
    }

    public Page<CourseDTO> searchCourses(String query, Pageable pageable) {
        return searchService.searchCourses(query, pageable)
                .map(course -> modelMapper.map(course, CourseDTO.class));
    }

    public List<CourseDTO> getCoursesByInstructor(Long instructorId) {
        return courseRepository.findByInstructorId(instructorId).stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .collect(Collectors.toList());
    }

    public List<CourseDTO> getCoursesByCategory(Long categoryId) {
        return courseRepository.findByCategoryId(categoryId).stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .collect(Collectors.toList());
    }

    public boolean hasAccessToLesson(Long userId, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        // Check if lesson is preview (free access)
        if (lesson.getIsPreview()) {
            return true;
        }

        // Check if user is enrolled in the course
        return enrollmentRepository.existsByUserIdAndCourseId(
                userId,
                lesson.getModule().getCourse().getId());
    }

    private BigDecimal calculateProgress(List<LessonProgress> progress) {
        if (progress.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long completedCount = progress.stream()
                .filter(p -> p.getStatus() == LessonProgress.ProgressStatus.COMPLETED)
                .count();

        return BigDecimal.valueOf(completedCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(progress.size()), 2, RoundingMode.HALF_UP);
    }
}
