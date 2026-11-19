package com.bugtracker.project.controller;

import com.bugtracker.common.dto.ApiResponse;
import com.bugtracker.project.dto.*;
import com.bugtracker.project.service.ProjectService;
import com.bugtracker.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project and project team membership management")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create a new project (Admin or Manager)")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Project created successfully",
                        projectService.createProject(request, userDetails.getUser())));
    }

    @GetMapping
    @Operation(summary = "Get all projects accessible to current user")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAllProjects(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getAccessibleProjects(userDetails.getUser())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project details by ID")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getProjectById(id, userDetails.getUser())));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project details")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Project updated successfully",
                projectService.updateProject(id, request, userDetails.getUser())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService.deleteProject(id, userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok("Project deleted successfully", null));
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add member to project")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> addMember(
            @PathVariable Long id,
            @Valid @RequestBody AddProjectMemberRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Member added to project",
                        projectService.addMember(id, request, userDetails.getUser())));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "Remove member from project")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService.removeMember(id, userId, userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok("Member removed from project", null));
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "List all members of a project")
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> getMembers(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getProjectMembers(id, userDetails.getUser())));
    }
}
