package com.bugtracker.project.dto;

import com.bugtracker.project.entity.ProjectRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddProjectMemberRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    private ProjectRole projectRole = ProjectRole.MEMBER;
}
