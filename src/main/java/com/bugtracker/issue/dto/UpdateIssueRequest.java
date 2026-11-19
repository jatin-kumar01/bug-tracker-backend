package com.bugtracker.issue.dto;

import com.bugtracker.issue.enums.IssuePriority;
import com.bugtracker.issue.enums.IssueStatus;
import com.bugtracker.issue.enums.IssueType;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateIssueRequest {
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    private String description;
    private IssueType type;
    private IssuePriority priority;
    private IssueStatus status;
    private Long assigneeId;
    private LocalDate dueDate;
}
