package com.bugtracker.comment.service;

import com.bugtracker.comment.dto.CommentRequest;
import com.bugtracker.comment.dto.CommentResponse;
import com.bugtracker.comment.entity.Comment;
import com.bugtracker.comment.repository.CommentRepository;
import com.bugtracker.exception.ForbiddenOperationException;
import com.bugtracker.exception.ResourceNotFoundException;
import com.bugtracker.issue.entity.Issue;
import com.bugtracker.issue.service.IssueService;
import com.bugtracker.project.service.ProjectService;
import com.bugtracker.user.entity.Role;
import com.bugtracker.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final IssueService issueService;
    private final ProjectService projectService;

    @Transactional
    public CommentResponse addComment(Long issueId, CommentRequest request, User currentUser) {
        Issue issue = issueService.findIssueEntity(issueId);
        projectService.validateProjectAccess(issue.getProject().getId(), currentUser);

        Comment comment = Comment.builder()
                .content(request.getContent().trim())
                .issue(issue)
                .author(currentUser)
                .build();

        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getIssueComments(Long issueId, User currentUser) {
        Issue issue = issueService.findIssueEntity(issueId);
        projectService.validateProjectAccess(issue.getProject().getId(), currentUser);

        return commentRepository.findByIssueIdOrderByCreatedAtAsc(issueId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request, User currentUser) {
        Comment comment = findCommentEntity(commentId);
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You can only edit your own comments.");
        }
        comment.setContent(request.getContent().trim());
        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long commentId, User currentUser) {
        Comment comment = findCommentEntity(commentId);
        if (currentUser.getRole() != Role.ADMIN && !comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You can only delete your own comments.");
        }
        commentRepository.delete(comment);
    }

    private Comment findCommentEntity(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
    }
}
