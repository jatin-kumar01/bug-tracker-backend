package com.bugtracker.user.dto;

import com.bugtracker.user.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRoleRequest {
    @NotNull(message = "Role is required")
    private Role role;
}
