package com.bugtracker.issue;

import com.bugtracker.exception.ForbiddenOperationException;
import com.bugtracker.issue.dto.CreateIssueRequest;
import com.bugtracker.issue.dto.IssueResponse;
import com.bugtracker.issue.entity.Issue;
import com.bugtracker.issue.enums.IssuePriority;
import com.bugtracker.issue.enums.IssueStatus;
import com.bugtracker.issue.enums.IssueType;
import com.bugtracker.issue.repository.IssueRepository;
import com.bugtracker.issue.service.IssueService;
import com.bugtracker.project.entity.Project;
import com.bugtracker.project.repository.ProjectMemberRepository;
import com.bugtracker.project.repository.ProjectRepository;
import com.bugtracker.project.service.ProjectService;
import com.bugtracker.user.entity.Role;
import com.bugtracker.user.entity.User;
import com.bugtracker.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @Mock private IssueRepository issueRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMemberRepository projectMemberRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProjectService projectService;

    @InjectMocks
    private IssueService issueService;

    @Test
    void createIssue_generatesSequentialProjectIssueKey() {
        User reporter = User.builder().id(1L).fullName("Jatin").email("jatin@test.com").role(Role.MANAGER).build();
        Project project = Project.builder().id(10L).projectKey("BUG").name("Core App").issueSequence(100).createdBy(reporter).build();

        CreateIssueRequest req = new CreateIssueRequest();
        req.setProjectId(10L);
        req.setTitle("Fix NullPointerException on login");
        req.setType(IssueType.BUG);
        req.setPriority(IssuePriority.HIGH);

        when(projectService.findProjectEntity(10L)).thenReturn(project);
        doNothing().when(projectService).validateProjectAccess(10L, reporter);
        when(issueRepository.save(any(Issue.class))).thenAnswer(inv -> {
            Issue i = inv.getArgument(0);
            i.setId(50L);
            return i;
        });

        IssueResponse response = issueService.createIssue(req, reporter);

        assertEquals("BUG-101", response.getIssueKey());
        assertEquals(IssueStatus.TODO, response.getStatus());
        verify(projectRepository).save(project);
    }

    @Test
    void updateIssueStatus_byUnrelatedDeveloper_throwsForbidden() {
        User reporter = User.builder().id(1L).fullName("Reporter").role(Role.DEVELOPER).build();
        User otherDev = User.builder().id(2L).fullName("Other Dev").role(Role.DEVELOPER).build();
        Project project = Project.builder().id(10L).projectKey("BUG").build();
        Issue issue = Issue.builder().id(5L).issueKey("BUG-105").project(project).reporter(reporter).status(IssueStatus.TODO).build();

        when(issueRepository.findById(5L)).thenReturn(Optional.of(issue));
        doNothing().when(projectService).validateProjectAccess(10L, otherDev);

        assertThrows(ForbiddenOperationException.class,
                () -> issueService.updateIssueStatus(5L, IssueStatus.IN_PROGRESS, otherDev));
    }
}
