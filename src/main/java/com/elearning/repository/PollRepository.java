package com.elearning.repository;

import com.elearning.model.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PollRepository extends JpaRepository<Poll, String> {
    List<Poll> findByClassroomId(String classroomId);
    
    @Query("SELECT p FROM Poll p WHERE p.classroom.id = :classroomId AND p.expiresAt > :now")
    List<Poll> findActivePollsByClassroom(@Param("classroomId") String classroomId, @Param("now") LocalDateTime now);
    
    @Query("SELECT p FROM Poll p WHERE p.expiresAt < :now")
    List<Poll> findExpiredPolls(@Param("now") LocalDateTime now);
}
