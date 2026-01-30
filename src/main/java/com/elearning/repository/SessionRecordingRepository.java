package com.elearning.repository;

import com.elearning.model.SessionRecording;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRecordingRepository extends JpaRepository<SessionRecording, Long> {
    List<SessionRecording> findByClassroomId(String classroomId);
    List<SessionRecording> findByClassroomIdAndStatus(String classroomId, SessionRecording.RecordingStatus status);
}
