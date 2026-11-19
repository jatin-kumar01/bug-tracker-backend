package com.bugtracker.project.service;

import com.bugtracker.exception.DuplicateResourceException;
import com.bugtracker.exception.ForbiddenOperationException;
import com.bugtracker.exception.ResourceNotFoundException;
import com.bugtracker.project.dto.*;
import com.bugtracker.project.entity.Project;
import com.bugtracker.project.entity.ProjectMember;
import com.bugtracker.project.entity.ProjectRole;
import com.bugtracker.project.repository.ProjectMemberRepository;
import com.bugtracker.project.repository.ProjectRepository;
import com.bugtracker.user.entity.Role;
import com.bugtracker.user.entity.User;
import com.bugtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, User currentUser) {
        String key = request.getProjectKey().trim().toUpperCase();
        if (projectRepository.existsByProjectKeyIgnoreCase(key)) {
            throw new DuplicateResourceException("Project key already exists: " + key);
        }

        Project project = Project.builder()
                .projectKey(key)
                .name(request.getName().trim())
                .description(request.getDescription())
                .issueSequence(100)
                .createdBy(currentUser)
                .build();

        Project savedProject = projectRepository.save(project);

        ProjectMember creatorMember = ProjectMember.builder()
                .project(savedProject)
                .user(currentUser)
                .projectRole(ProjectRole.PROJECT_LEAD)
                .build();
        projectMemberRepository.save(creatorMember);

        return ProjectResponse.from(savedProject);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAccessibleProjects(User currentUser) {
        List<Project> projects = (currentUser.getRole() == Role.ADMIN)
                ? projectRepository.findAll()
                : projectRepository.findAllByMemberUserId(currentUser.getId());
        return projects.stream().map(ProjectResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId, User currentUser) {
        Project project = findProjectEntity(projectId);
        validateProjectAccess(projectId, currentUser);
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request, User currentUser) {
        Project project = findProjectEntity(projectId);
        validateProjectManagementAccess(projectId, currentUser);

        if (request.getName() != null && !request.getName().isBlank()) {
            project.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(Long projectId, User currentUser) {
        Project project = findProjectEntity(projectId);
        if (currentUser.getRole() != Role.ADMIN && !project.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("Only an Admin or Project Creator can delete this project.");
        }
        projectRepository.delete(project);
    }

    @Transactional
    public ProjectMemberResponse addMember(Long projectId, AddProjectMemberRequest request, User currentUser) {
        Project project = findProjectEntity(projectId);
        validateProjectManagementAccess(projectId, currentUser);

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, request.getUserId())) {
            throw new DuplicateResourceException("User is already a member of this project.");
        }

        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(targetUser)
                .projectRole(request.getProjectRole() != null ? request.getProjectRole() : ProjectRole.MEMBER)
                .build();

        return ProjectMemberResponse.from(projectMemberRepository.save(member));
    }

    @Transactional
    public void removeMember(Long projectId, Long userId, User currentUser) {
        Project project = findProjectEntity(projectId);
        validateProjectManagementAccess(projectId, currentUser);

        if (project.getCreatedBy().getId().equals(userId)) {
            throw new ForbiddenOperationException("Cannot remove the project creator from the project.");
        }

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this project."));

        projectMemberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getProjectMembers(Long projectId, User currentUser) {
        findProjectEntity(projectId);
        validateProjectAccess(projectId, currentUser);
        return projectMemberRepository.findByProjectId(projectId).stream()
                .map(ProjectMemberResponse::from)
                .toList();
    }

    public Project findProjectEntity(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
    }

    public void validateProjectAccess(Long projectId, User user) {
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
            throw new ForbiddenOperationException("You do not have access to this project.");
        }
    }

    public void validateProjectManagementAccess(Long projectId, User user) {
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new ForbiddenOperationException("You are not a member of this project."));
        if (user.getRole() != Role.MANAGER && member.getProjectRole() != ProjectRole.PROJECT_LEAD) {
            throw new ForbiddenOperationException("Only Admins, Managers, or Project Leads can manage project settings.");
        }
    }
}
