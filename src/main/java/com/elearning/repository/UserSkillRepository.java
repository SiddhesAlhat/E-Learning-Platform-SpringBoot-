package com.elearning.repository;

import com.elearning.model.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
    List<UserSkill> findByUserId(Long userId);
    List<UserSkill> findByUserIdAndProficiencyLevel(Long userId, UserSkill.ProficiencyLevel proficiencyLevel);
    void deleteByUserId(Long userId);
}
