package com.bugtracker.attachment.controller;

import com.bugtracker.attachment.dto.AttachmentResponse;
import com.bugtracker.attachment.entity.Attachment;
import com.bugtracker.attachment.service.AttachmentService;
import com.bugtracker.common.dto.ApiResponse;
import com.bugtracker.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Attachments", description = "Upload and download issue screenshots and files")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(value = "/issues/{issueId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload screenshot or document to an issue")
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadAttachment(
            @PathVariable Long issueId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("File uploaded successfully",
                        attachmentService.uploadAttachment(issueId, file, userDetails.getUser())));
    }

    @GetMapping("/issues/{issueId}/attachments")
    @Operation(summary = "List all attachments on an issue")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getIssueAttachments(
            @PathVariable Long issueId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                attachmentService.getIssueAttachments(issueId, userDetails.getUser())));
    }

    @GetMapping("/attachments/{attachmentId}/download")
    @Operation(summary = "Download an attachment by ID")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Attachment attachment = attachmentService.getAttachmentMetadata(attachmentId, userDetails.getUser());
        Resource resource = attachmentService.loadAttachmentAsResource(attachment);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(resource);
    }
}
