package com.elearning.repository;

import com.elearning.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findByCourseIdOrderBySequenceOrder(Long courseId);
    List<Module> findByCourseId(Long courseId);
    void deleteByCourseId(Long courseId);
}
