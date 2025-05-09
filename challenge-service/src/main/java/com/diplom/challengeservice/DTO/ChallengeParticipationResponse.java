package com.diplom.challengeservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChallengeParticipationResponse {
    private Long challengeId;
    private boolean isParticipant;
    private boolean isCreator;
}