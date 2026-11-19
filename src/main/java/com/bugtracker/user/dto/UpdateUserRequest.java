package com.bugtracker.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Size(min = 2, max = 120, message = "Full name must be between 2 and 120 characters")
    private String fullName;

    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;
}
