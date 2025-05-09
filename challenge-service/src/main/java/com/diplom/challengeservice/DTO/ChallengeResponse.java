package com.diplom.challengeservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeResponse {
    private Long id;
    private String title;
    private Integer targetXP;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long creatorId;
    private String creatorName;
}