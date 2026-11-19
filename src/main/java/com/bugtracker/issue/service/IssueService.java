package com.bugtracker.issue.service;

import com.bugtracker.common.dto.PagedResponse;
import com.bugtracker.exception.BadRequestException;
import com.bugtracker.exception.ForbiddenOperationException;
import com.bugtracker.exception.ResourceNotFoundException;
import com.bugtracker.issue.dto.*;
import com.bugtracker.issue.entity.Issue;
import com.bugtracker.issue.enums.IssuePriority;
import com.bugtracker.issue.enums.IssueStatus;
import com.bugtracker.issue.enums.IssueType;
import com.bugtracker.issue.repository.IssueRepository;
import com.bugtracker.issue.repository.IssueSpecification;
import com.bugtracker.project.entity.Project;
import com.bugtracker.project.repository.ProjectMemberRepository;
import com.bugtracker.project.repository.ProjectRepository;
import com.bugtracker.project.service.ProjectService;
import com.bugtracker.user.entity.Role;
import com.bugtracker.user.entity.User;
import com.bugtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;

    @Transactional
    public IssueResponse createIssue(CreateIssueRequest request, User currentUser) {
        Project project = projectService.findProjectEntity(request.getProjectId());
        projectService.validateProjectAccess(project.getId(), currentUser);

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = validateAndGetProjectAssignee(project.getId(), request.getAssigneeId());
        }

        int nextNumber = project.nextIssueNumber();
        projectRepository.save(project);

        Issue issue = Issue.builder()
                .issueKey(project.getProjectKey() + "-" + nextNumber)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .type(request.getType())
                .priority(request.getPriority())
                .status(IssueStatus.TODO)
                .project(project)
                .reporter(currentUser)
                .assignee(assignee)
                .dueDate(request.getDueDate())
                .build();

        return IssueResponse.from(issueRepository.save(issue));
    }

    @Transactional(readOnly = true)
    public PagedResponse<IssueResponse> getIssues(
            Long projectId,
            IssueStatus status,
            IssuePriority priority,
            IssueType type,
            Long assigneeId,
            String search,
            Pageable pageable,
            User currentUser) {

        List<Long> allowedProjectIds = null;
        if (currentUser.getRole() != Role.ADMIN) {
            allowedProjectIds = projectRepository.findAllByMemberUserId(currentUser.getId())
                    .stream()
                    .map(Project::getId)
                    .toList();
        }

        if (projectId != null) {
            projectService.validateProjectAccess(projectId, currentUser);
        }

        Page<IssueResponse> page = issueRepository.findAll(
                IssueSpecification.withFilters(allowedProjectIds, projectId, status, priority, type, assigneeId, search),
                pageable
        ).map(IssueResponse::from);

        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public IssueResponse getIssueById(Long issueId, User currentUser) {
        Issue issue = findIssueEntity(issueId);
        projectService.validateProjectAccess(issue.getProject().getId(), currentUser);
        return IssueResponse.from(issue);
    }

    @Transactional
    public IssueResponse updateIssue(Long issueId, UpdateIssueRequest request, User currentUser) {
        Issue issue = findIssueEntity(issueId);
        projectService.validateProjectAccess(issue.getProject().getId(), currentUser);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            issue.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            issue.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            issue.setType(request.getType());
        }
        if (request.getPriority() != null) {
            issue.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            issue.setStatus(request.getStatus());
        }
        if (request.getAssigneeId() != null) {
            User assignee = validateAndGetProjectAssignee(issue.getProject().getId(), request.getAssigneeId());
            issue.setAssignee(assignee);
        }
        if (request.getDueDate() != null) {
            issue.setDueDate(request.getDueDate());
        }

        return IssueResponse.from(issueRepository.save(issue));
    }

    @Transactional
    public IssueResponse updateIssueStatus(Long issueId, IssueStatus newStatus, User currentUser) {
        Issue issue = findIssueEntity(issueId);
        Long projectId = issue.getProject().getId();
        projectService.validateProjectAccess(projectId, currentUser);

        boolean isPrivileged = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.MANAGER;
        boolean isReporter = issue.getReporter().getId().equals(currentUser.getId());
        boolean isAssignee = issue.getAssignee() != null && issue.getAssignee().getId().equals(currentUser.getId());

        if (!isPrivileged && !isReporter && !isAssignee) {
            throw new ForbiddenOperationException("Only Admins, Managers, the Reporter, or the Assignee can change issue status.");
        }

        issue.setStatus(newStatus);
        return IssueResponse.from(issueRepository.save(issue));
    }

    @Transactional
    public IssueResponse assignIssue(Long issueId, Long assigneeId, User currentUser) {
        Issue issue = findIssueEntity(issueId);
        Long projectId = issue.getProject().getId();
        projectService.validateProjectAccess(projectId, currentUser);

        if (assigneeId == null) {
            issue.setAssignee(null);
        } else {
            User assignee = validateAndGetProjectAssignee(projectId, assigneeId);
            issue.setAssignee(assignee);
        }
        return IssueResponse.from(issueRepository.save(issue));
    }

    @Transactional
    public void deleteIssue(Long issueId, User currentUser) {
        Issue issue = findIssueEntity(issueId);
        Long projectId = issue.getProject().getId();
        projectService.validateProjectAccess(projectId, currentUser);

        boolean isPrivileged = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.MANAGER;
        boolean isReporter = issue.getReporter().getId().equals(currentUser.getId());

        if (!isPrivileged && !isReporter) {
            throw new ForbiddenOperationException("Only Admins, Managers, or the Issue Reporter can delete this issue.");
        }
        issueRepository.delete(issue);
    }

    @Transactional(readOnly = true)
    public KanbanBoardResponse getProjectKanbanBoard(Long projectId, User currentUser) {
        Project project = projectService.findProjectEntity(projectId);
        projectService.validateProjectAccess(projectId, currentUser);

        List<Issue> issues = issueRepository.findByProjectIdOrderByCreatedAtAsc(projectId);

        Map<IssueStatus, List<IssueResponse>> columns = new EnumMap<>(IssueStatus.class);
        for (IssueStatus status : IssueStatus.values()) {
            columns.put(status, issues.stream()
                    .filter(i -> i.getStatus() == status)
                    .map(IssueResponse::from)
                    .toList());
        }

        return KanbanBoardResponse.builder()
                .projectId(project.getId())
                .projectKey(project.getProjectKey())
                .projectName(project.getName())
                .columns(columns)
                .build();
    }

    public Issue findIssueEntity(Long issueId) {
        return issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + issueId));
    }

    private User validateAndGetProjectAssignee(Long projectId, Long assigneeId) {
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignee user not found with id: " + assigneeId));
        if (!assignee.isActive()) {
            throw new BadRequestException("Cannot assign issue to a deactivated user.");
        }
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, assigneeId)) {
            throw new BadRequestException("Assignee must be a member of the project.");
        }
        return assignee;
    }
}
