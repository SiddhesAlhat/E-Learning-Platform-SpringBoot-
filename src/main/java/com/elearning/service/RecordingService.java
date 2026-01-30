package com.elearning.service;

import com.elearning.model.SessionRecording;
import com.elearning.repository.SessionRecordingRepository;
import com.elearning.repository.VirtualClassroomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecordingService {

    @Autowired
    private SessionRecordingRepository sessionRecordingRepository;

    @Autowired
    private VirtualClassroomRepository virtualClassroomRepository;

    public SessionRecording startSessionRecording(String sessionId) {
        SessionRecording recording = SessionRecording.builder()
                .classroomId(Long.parseLong(sessionId))
                .status(SessionRecording.RecordingStatus.PROCESSING)
                .build();

        return sessionRecordingRepository.save(recording);
    }

    public void stopSessionRecording(String sessionId) {
        // Find the active recording for this session
        SessionRecording recording = sessionRecordingRepository
                .findByClassroomIdAndStatus(sessionId, SessionRecording.RecordingStatus.PROCESSING)
                .stream()
                .findFirst()
                .orElse(null);

        if (recording != null) {
            recording.setStatus(SessionRecording.RecordingStatus.READY);
            recording.setStorageUrl("https://cdn.example.com/recordings/" + recording.getId() + ".mp4");
            recording.setDuration(3600); // Example duration
            recording.setFileSize(50000000L); // Example file size
            sessionRecordingRepository.save(recording);
        }
    }
}
