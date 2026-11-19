package com.bugtracker.project.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProjectRequest {
    @Size(min = 2, max = 150, message = "Project name must be between 2 and 150 characters")
    private String name;

    private String description;
}
