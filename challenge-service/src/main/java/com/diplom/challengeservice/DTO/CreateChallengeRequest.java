package com.diplom.challengeservice.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateChallengeRequest {
    private String title;
    private Integer targetXP;
    private Integer durationDays;
    private List<Long> participantIds;
    private String teamName;
}