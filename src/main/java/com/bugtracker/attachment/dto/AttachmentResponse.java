package com.bugtracker.attachment.dto;

import com.bugtracker.attachment.entity.Attachment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Long issueId;
    private Long uploadedById;
    private String uploadedByName;
    private Instant createdAt;

    public static AttachmentResponse from(Attachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .issueId(attachment.getIssue().getId())
                .uploadedById(attachment.getUploadedBy().getId())
                .uploadedByName(attachment.getUploadedBy().getFullName())
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}
