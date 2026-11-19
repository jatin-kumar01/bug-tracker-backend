package com.bugtracker.issue.dto;

import com.bugtracker.issue.enums.IssuePriority;
import com.bugtracker.issue.enums.IssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateIssueRequest {
    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotBlank(message = "Issue title is required")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    private String description;

    @NotNull(message = "Issue type is required")
    private IssueType type;

    @NotNull(message = "Priority is required")
    private IssuePriority priority;

    private Long assigneeId;

    private LocalDate dueDate;
}
