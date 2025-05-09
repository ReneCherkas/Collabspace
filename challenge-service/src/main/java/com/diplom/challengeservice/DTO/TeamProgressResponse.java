package com.diplom.challengeservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TeamProgressResponse {
    private Long userId;
    private String userName;
    private String avatarUrl;
    private Long teamId;
    private String teamName;
    private Integer initialXP;
    private Integer currentXP;
    private Integer xpGained;
}