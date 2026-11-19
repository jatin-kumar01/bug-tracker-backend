package com.bugtracker.attachment.service;

import com.bugtracker.attachment.dto.AttachmentResponse;
import com.bugtracker.attachment.entity.Attachment;
import com.bugtracker.attachment.repository.AttachmentRepository;
import com.bugtracker.exception.BadRequestException;
import com.bugtracker.exception.ResourceNotFoundException;
import com.bugtracker.issue.entity.Issue;
import com.bugtracker.issue.service.IssueService;
import com.bugtracker.project.service.ProjectService;
import com.bugtracker.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/webp", "application/pdf", "text/plain"
    );

    private final AttachmentRepository attachmentRepository;
    private final IssueService issueService;
    private final ProjectService projectService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Transactional
    public AttachmentResponse uploadAttachment(Long issueId, MultipartFile file, User currentUser) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be empty.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Unsupported file type. Allowed: PNG, JPEG, WEBP, PDF, TXT.");
        }

        Issue issue = issueService.findIssueEntity(issueId);
        projectService.validateProjectAccess(issue.getProject().getId(), currentUser);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        String storedFileName = UUID.randomUUID() + "_" + originalFilename;

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            Path targetLocation = uploadPath.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            Attachment attachment = Attachment.builder()
                    .fileName(originalFilename)
                    .storedFileName(storedFileName)
                    .fileType(contentType)
                    .fileSize(file.getSize())
                    .storagePath(targetLocation.toString())
                    .issue(issue)
                    .uploadedBy(currentUser)
                    .build();

            return AttachmentResponse.from(attachmentRepository.save(attachment));
        } catch (IOException e) {
            throw new RuntimeException("Failed to store uploaded file: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> getIssueAttachments(Long issueId, User currentUser) {
        Issue issue = issueService.findIssueEntity(issueId);
        projectService.validateProjectAccess(issue.getProject().getId(), currentUser);
        return attachmentRepository.findByIssueIdOrderByCreatedAtDesc(issueId).stream()
                .map(AttachmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Attachment getAttachmentMetadata(Long attachmentId, User currentUser) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + attachmentId));
        projectService.validateProjectAccess(attachment.getIssue().getProject().getId(), currentUser);
        return attachment;
    }

    public Resource loadAttachmentAsResource(Attachment attachment) {
        try {
            Path filePath = Paths.get(attachment.getStoragePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new ResourceNotFoundException("Attachment file not found on disk.");
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Invalid attachment storage path.");
        }
    }
}
