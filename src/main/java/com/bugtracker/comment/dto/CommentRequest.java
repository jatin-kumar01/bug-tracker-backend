package com.bugtracker.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequest {
    @NotBlank(message = "Comment content cannot be empty")
    @Size(max = 5000, message = "Comment cannot exceed 5000 characters")
    private String content;
}
