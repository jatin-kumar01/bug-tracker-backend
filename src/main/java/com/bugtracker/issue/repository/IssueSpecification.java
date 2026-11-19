package com.bugtracker.issue.repository;

import com.bugtracker.issue.entity.Issue;
import com.bugtracker.issue.enums.IssuePriority;
import com.bugtracker.issue.enums.IssueStatus;
import com.bugtracker.issue.enums.IssueType;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class IssueSpecification {

    private IssueSpecification() {}

    public static Specification<Issue> withFilters(
            List<Long> allowedProjectIds,
            Long projectId,
            IssueStatus status,
            IssuePriority priority,
            IssueType type,
            Long assigneeId,
            String search) {

        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (allowedProjectIds != null) {
                if (allowedProjectIds.isEmpty()) {
                    return cb.disjunction();
                }
                predicates = cb.and(predicates, root.get("project").get("id").in(allowedProjectIds));
            }
            if (projectId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("project").get("id"), projectId));
            }
            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates = cb.and(predicates, cb.equal(root.get("priority"), priority));
            }
            if (type != null) {
                predicates = cb.and(predicates, cb.equal(root.get("type"), type));
            }
            if (assigneeId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("assignee").get("id"), assigneeId));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates = cb.and(predicates, cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("issueKey")), pattern)
                ));
            }
            return predicates;
        };
    }
}
