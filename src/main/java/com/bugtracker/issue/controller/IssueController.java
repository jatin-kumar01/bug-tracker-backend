package com.bugtracker.issue.controller;

import com.bugtracker.common.dto.ApiResponse;
import com.bugtracker.common.dto.PagedResponse;
import com.bugtracker.issue.dto.*;
import com.bugtracker.issue.enums.IssuePriority;
import com.bugtracker.issue.enums.IssueStatus;
import com.bugtracker.issue.enums.IssueType;
import com.bugtracker.issue.service.IssueService;
import com.bugtracker.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Issues & Kanban", description = "Issue tracking, filtering, assignment, and Kanban board operations")
public class IssueController {

    private final IssueService issueService;

    @PostMapping("/issues")
    @Operation(summary = "Create a new issue in a project")
    public ResponseEntity<ApiResponse<IssueResponse>> createIssue(
            @Valid @RequestBody CreateIssueRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Issue created successfully",
                        issueService.createIssue(request, userDetails.getUser())));
    }

    @GetMapping("/issues")
    @Operation(summary = "Search and filter issues with pagination and sorting")
    public ResponseEntity<ApiResponse<PagedResponse<IssueResponse>>> getIssues(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) IssuePriority priority,
            @RequestParam(required = false) IssueType type,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(ApiResponse.ok(
                issueService.getIssues(projectId, status, priority, type, assigneeId, search, pageable, userDetails.getUser())
        ));
    }

    @GetMapping("/issues/{id}")
    @Operation(summary = "Get issue details by ID")
    public ResponseEntity<ApiResponse<IssueResponse>> getIssueById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(issueService.getIssueById(id, userDetails.getUser())));
    }

    @PutMapping("/issues/{id}")
    @Operation(summary = "Update issue fields")
    public ResponseEntity<ApiResponse<IssueResponse>> updateIssue(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIssueRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Issue updated successfully",
                issueService.updateIssue(id, request, userDetails.getUser())));
    }

    @PatchMapping("/issues/{id}/status")
    @Operation(summary = "Update issue status (supports Kanban drag-and-drop)")
    public ResponseEntity<ApiResponse<IssueResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIssueStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Issue status updated",
                issueService.updateIssueStatus(id, request.getStatus(), userDetails.getUser())));
    }

    @PatchMapping("/issues/{id}/assignee")
    @Operation(summary = "Assign or unassign an issue to a developer")
    public ResponseEntity<ApiResponse<IssueResponse>> assignIssue(
            @PathVariable Long id,
            @RequestBody AssignIssueRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Issue assignee updated",
                issueService.assignIssue(id, request.getAssigneeId(), userDetails.getUser())));
    }

    @DeleteMapping("/issues/{id}")
    @Operation(summary = "Delete an issue")
    public ResponseEntity<ApiResponse<Void>> deleteIssue(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        issueService.deleteIssue(id, userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok("Issue deleted successfully", null));
    }

    @GetMapping("/projects/{projectId}/board")
    @Operation(summary = "Get Kanban board columns grouped by status for a project")
    public ResponseEntity<ApiResponse<KanbanBoardResponse>> getKanbanBoard(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                issueService.getProjectKanbanBoard(projectId, userDetails.getUser())));
    }
}
