package com.bugtracker.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateProjectRequest {
    @NotBlank(message = "Project key is required")
    @Size(min = 2, max = 10, message = "Project key must be 2 to 10 characters")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Project key must be alphanumeric")
    private String projectKey;

    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 150, message = "Project name must be between 2 and 150 characters")
    private String name;

    private String description;
}
