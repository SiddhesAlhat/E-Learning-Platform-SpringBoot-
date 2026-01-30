package com.elearning.controller;

import com.elearning.dto.*;
import com.elearning.model.*;
import com.elearning.service.CourseService;
import com.elearning.service.EnrollmentService;
import com.elearning.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@Slf4j
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:3000" })
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private UserService userService;

    // Get all published courses
    @GetMapping
    public ResponseEntity<Page<CourseDTO>> getAllPublishedCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<CourseDTO> courses = courseService.getPublishedCourses(pageable);
        return ResponseEntity.ok(courses);
    }

    // Get course details with progress
    @GetMapping("/{courseId}")
    public ResponseEntity<CourseDetailDTO> getCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername());
        CourseDetailDTO course = courseService.getCourseWithProgress(courseId, user.getId());
        return ResponseEntity.ok(course);
    }

    // Create new course (instructor only)
    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<CourseDTO> createCourse(
            @Valid @RequestBody CreateCourseRequest request,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        User instructor = userService.findByUsername(userDetails.getUsername());
        CourseDTO course = courseService.createCourse(request, instructor);

        return ResponseEntity.status(HttpStatus.CREATED).body(course);
    }

    // Create course with file upload
    @PostMapping("/with-thumbnail")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<CourseDTO> createCourseWithThumbnail(
            @RequestPart("course") @Valid CreateCourseRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        User instructor = userService.findByUsername(userDetails.getUsername());
        request.setThumbnail(thumbnail);
        CourseDTO course = courseService.createCourse(request, instructor);

        return ResponseEntity.status(HttpStatus.CREATED).body(course);
    }

    // Update course (instructor only)
    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateCourseRequest request,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        User instructor = userService.findByUsername(userDetails.getUsername());
        CourseDTO course = courseService.updateCourse(courseId, request, instructor);

        return ResponseEntity.ok(course);
    }

    // Delete course (instructor only)
    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User instructor = userService.findByUsername(userDetails.getUsername());
        courseService.deleteCourse(courseId, instructor);

        return ResponseEntity.noContent().build();
    }

    // Add module to course
    @PostMapping("/{courseId}/modules")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ModuleDTO> addModule(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateModuleRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        ModuleDTO module = courseService.addModuleToCourse(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(module);
    }

    // Add lesson to module
    @PostMapping("/{courseId}/modules/{moduleId}/lessons")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<LessonDTO> addLesson(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @Valid @RequestBody CreateLessonRequest request,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        LessonDTO lesson = courseService.addLessonToModule(moduleId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(lesson);
    }

    // Upload video lesson
    @PostMapping("/{courseId}/modules/{moduleId}/lessons/video")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<LessonDTO> uploadVideoLesson(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isPreview", defaultValue = "false") boolean isPreview,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        CreateLessonRequest request = new CreateLessonRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setContentType(Lesson.ContentType.VIDEO);
        request.setContentFile(file);
        request.setIsPreview(isPreview);

        LessonDTO lesson = courseService.addLessonToModule(moduleId, request);
        return ResponseEntity.ok(lesson);
    }

    // Upload document lesson
    @PostMapping("/{courseId}/modules/{moduleId}/lessons/document")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<LessonDTO> uploadDocumentLesson(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        CreateLessonRequest request = new CreateLessonRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setContentType(Lesson.ContentType.DOCUMENT);
        request.setContentFile(file);

        LessonDTO lesson = courseService.addLessonToModule(moduleId, request);
        return ResponseEntity.ok(lesson);
    }

    // Get video streaming URL
    @GetMapping("/lessons/{lessonId}/stream")
    public ResponseEntity<VideoStreamDTO> getVideoStream(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername());

        // Verify user has access to this lesson
        if (!courseService.hasAccessToLesson(user.getId(), lessonId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // This would generate a signed URL or token for video streaming
        VideoStreamDTO streamData = new VideoStreamDTO();
        streamData.setLessonId(lessonId);
        streamData.setStreamUrl("https://cdn.example.com/video/" + lessonId + "/playlist.m3u8");
        streamData.setToken("generated-jwt-token");

        return ResponseEntity.ok(streamData);
    }

    // Search courses
    @GetMapping("/search")
    public ResponseEntity<Page<CourseDTO>> searchCourses(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<CourseDTO> courses = courseService.searchCourses(query, pageable);
        return ResponseEntity.ok(courses);
    }

    // Get courses by category
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<CourseDTO>> getCoursesByCategory(
            @PathVariable Long categoryId) {

        List<CourseDTO> courses = courseService.getCoursesByCategory(categoryId);
        return ResponseEntity.ok(courses);
    }

    // Get instructor's courses
    @GetMapping("/instructor")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<List<CourseDTO>> getInstructorCourses(
            @AuthenticationPrincipal UserDetails userDetails) {

        User instructor = userService.findByUsername(userDetails.getUsername());
        List<CourseDTO> courses = courseService.getCoursesByInstructor(instructor.getId());
        return ResponseEntity.ok(courses);
    }

    // Enroll in course
    @PostMapping("/{courseId}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<EnrollmentDTO> enrollInCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User student = userService.findByUsername(userDetails.getUsername());
        EnrollmentDTO enrollment = enrollmentService.enrollStudent(student.getId(), courseId);

        return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
    }

    // Get enrolled courses
    @GetMapping("/enrolled")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<EnrollmentDTO>> getEnrolledCourses(
            @AuthenticationPrincipal UserDetails userDetails) {

        User student = userService.findByUsername(userDetails.getUsername());
        List<EnrollmentDTO> enrollments = enrollmentService.getStudentEnrollments(student.getId());

        return ResponseEntity.ok(enrollments);
    }

    // Legacy endpoints for backward compatibility
    @GetMapping("/legacy")
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @PostMapping("/legacy")
    public ResponseEntity<CourseDTO> createCourseLegacy(@RequestBody CourseDTO courseDTO) {
        // Legacy endpoint - would need to be updated
        return ResponseEntity.ok(courseDTO);
    }

    @GetMapping("/legacy/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        // Legacy endpoint - redirects to new implementation
        return ResponseEntity.ok(courseService.getAllCourses().get(0));
    }
}
