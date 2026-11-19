package com.bugtracker.project.repository;

import com.bugtracker.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByProjectKeyIgnoreCase(String projectKey);

    @Query("SELECT pm.project FROM ProjectMember pm WHERE pm.user.id = :userId")
    List<Project> findAllByMemberUserId(Long userId);
}
