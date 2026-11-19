package com.bugtracker.issue.dto;

import com.bugtracker.issue.entity.Issue;
import com.bugtracker.issue.enums.IssuePriority;
import com.bugtracker.issue.enums.IssueStatus;
import com.bugtracker.issue.enums.IssueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueResponse {
    private Long id;
    private String issueKey;
    private String title;
    private String description;
    private IssueType type;
    private IssuePriority priority;
    private IssueStatus status;
    private Long projectId;
    private String projectKey;
    private Long reporterId;
    private String reporterName;
    private Long assigneeId;
    private String assigneeName;
    private LocalDate dueDate;
    private Instant createdAt;
    private Instant updatedAt;

    public static IssueResponse from(Issue issue) {
        return IssueResponse.builder()
                .id(issue.getId())
                .issueKey(issue.getIssueKey())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .type(issue.getType())
                .priority(issue.getPriority())
                .status(issue.getStatus())
                .projectId(issue.getProject().getId())
                .projectKey(issue.getProject().getProjectKey())
                .reporterId(issue.getReporter().getId())
                .reporterName(issue.getReporter().getFullName())
                .assigneeId(issue.getAssignee() != null ? issue.getAssignee().getId() : null)
                .assigneeName(issue.getAssignee() != null ? issue.getAssignee().getFullName() : null)
                .dueDate(issue.getDueDate())
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .build();
    }
}
