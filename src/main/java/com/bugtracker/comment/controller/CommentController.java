package com.bugtracker.comment.controller;

import com.bugtracker.comment.dto.CommentRequest;
import com.bugtracker.comment.dto.CommentResponse;
import com.bugtracker.comment.service.CommentService;
import com.bugtracker.common.dto.ApiResponse;
import com.bugtracker.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Issue discussion threads")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/issues/{issueId}/comments")
    @Operation(summary = "Add a comment to an issue")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long issueId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Comment added",
                        commentService.addComment(issueId, request, userDetails.getUser())));
    }

    @GetMapping("/issues/{issueId}/comments")
    @Operation(summary = "Get all comments for an issue")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getIssueComments(
            @PathVariable Long issueId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                commentService.getIssueComments(issueId, userDetails.getUser())));
    }

    @PutMapping("/comments/{commentId}")
    @Operation(summary = "Update your own comment")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok("Comment updated",
                commentService.updateComment(commentId, request, userDetails.getUser())));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Delete your own comment (or Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        commentService.deleteComment(commentId, userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok("Comment deleted", null));
    }
}
