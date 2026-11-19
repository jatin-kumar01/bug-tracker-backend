package com.bugtracker.issue.dto;

import com.bugtracker.issue.enums.IssueStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanbanBoardResponse {
    private Long projectId;
    private String projectKey;
    private String projectName;
    private Map<IssueStatus, List<IssueResponse>> columns;
}
