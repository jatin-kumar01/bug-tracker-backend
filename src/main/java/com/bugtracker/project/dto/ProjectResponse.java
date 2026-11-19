package com.bugtracker.project.dto;

import com.bugtracker.project.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String projectKey;
    private String name;
    private String description;
    private Long createdById;
    private String createdByName;
    private Instant createdAt;
    private Instant updatedAt;

    public static ProjectResponse from(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .projectKey(project.getProjectKey())
                .name(project.getName())
                .description(project.getDescription())
                .createdById(project.getCreatedBy().getId())
                .createdByName(project.getCreatedBy().getFullName())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
