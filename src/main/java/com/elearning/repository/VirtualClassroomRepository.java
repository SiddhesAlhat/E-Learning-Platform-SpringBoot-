package com.elearning.repository;

import com.elearning.model.VirtualClassroom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VirtualClassroomRepository extends JpaRepository<VirtualClassroom, Long> {
    List<VirtualClassroom> findByIsActiveTrue();
}
