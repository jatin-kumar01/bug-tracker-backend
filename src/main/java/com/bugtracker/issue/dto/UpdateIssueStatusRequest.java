package com.bugtracker.issue.dto;

import com.bugtracker.issue.enums.IssueStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateIssueStatusRequest {
    @NotNull(message = "Status is required")
    private IssueStatus status;
}
