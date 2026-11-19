package com.bugtracker.project.dto;

import com.bugtracker.project.entity.ProjectMember;
import com.bugtracker.project.entity.ProjectRole;
import com.bugtracker.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private Role systemRole;
    private ProjectRole projectRole;
    private Instant joinedAt;

    public static ProjectMemberResponse from(ProjectMember member) {
        return ProjectMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .fullName(member.getUser().getFullName())
                .email(member.getUser().getEmail())
                .systemRole(member.getUser().getRole())
                .projectRole(member.getProjectRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
