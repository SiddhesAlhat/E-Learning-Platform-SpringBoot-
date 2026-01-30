package com.elearning.repository;

import com.elearning.model.VirtualClassroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VirtualClassroomRepository extends JpaRepository<VirtualClassroom, Long> {
    
    // Legacy method for backward compatibility
    List<VirtualClassroom> findByIsActiveTrue();
    
    @Query("SELECT vc FROM VirtualClassroom vc WHERE vc.status = 'LIVE'")
    List<VirtualClassroom> findActiveSessions();
    
    @Query("SELECT vc FROM VirtualClassroom vc WHERE vc.instructor.id = :instructorId AND vc.scheduledStartTime > :now ORDER BY vc.scheduledStartTime")
    List<VirtualClassroom> findUpcomingSessionsForInstructor(@Param("instructorId") Long instructorId, @Param("now") LocalDateTime now);
    
    @Query("SELECT vc FROM VirtualClassroom vc JOIN vc.course.enrollments e WHERE e.user.id = :userId AND vc.scheduledStartTime > :now ORDER BY vc.scheduledStartTime")
    List<VirtualClassroom> findUpcomingSessionsForUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    @Query("SELECT vc FROM VirtualClassroom vc WHERE vc.instructor.id = :instructorId AND vc.endTime < :now ORDER BY vc.endTime DESC")
    List<VirtualClassroom> findPastSessionsForInstructor(@Param("instructorId") Long instructorId, @Param("now") LocalDateTime now);
    
    @Query("SELECT vc FROM VirtualClassroom vc JOIN vc.course.enrollments e WHERE e.user.id = :userId AND vc.endTime < :now ORDER BY vc.endTime DESC")
    List<VirtualClassroom> findPastSessionsForUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    void incrementParticipantCount(Long sessionId);
    void decrementParticipantCount(Long sessionId);
}
