package com.elearning.repository;

import com.elearning.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") User.Role role);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = com.elearning.model.User$Role.INSTRUCTOR")
    List<User> findInstructors();

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = com.elearning.model.User$Role.STUDENT")
    List<User> findStudents();

    @Query("SELECT u FROM User u WHERE u.isActive = true")
    List<User> findActiveUsers();
}
