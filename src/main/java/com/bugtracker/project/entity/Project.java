package com.bugtracker.project.entity;

import com.bugtracker.common.entity.BaseEntity;
import com.bugtracker.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_key", nullable = false, unique = true, length = 15)
    private String projectKey;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "issue_sequence", nullable = false)
    @Builder.Default
    private Integer issueSequence = 100;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    public int nextIssueNumber() {
        this.issueSequence = (this.issueSequence == null ? 100 : this.issueSequence) + 1;
        return this.issueSequence;
    }
}
