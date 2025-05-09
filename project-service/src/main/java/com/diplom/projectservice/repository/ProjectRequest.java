package com.diplom.projectservice.repository;

import lombok.Data;
import java.util.List;

@Data
public class ProjectRequest {
    private String title;
    private String description;
    private List<Long> adminIds;
    private List<Long> teamMemberIds;
    private Long creatorId;
    private String login;
}