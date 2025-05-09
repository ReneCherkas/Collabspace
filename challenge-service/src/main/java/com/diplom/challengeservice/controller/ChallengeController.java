package com.diplom.challengeservice.controller;


import com.diplom.challengeservice.DTO.ChallengeResponse;
import com.diplom.challengeservice.DTO.CreateChallengeRequest;
import com.diplom.challengeservice.DTO.TeamProgressResponse;
import com.diplom.challengeservice.service.TaskService;
import com.diplom.challengeservice.service.XPService;
import com.diplom.challengeservice.model.*;
import com.diplom.challengeservice.repository.ChallengeRepository;
import com.diplom.challengeservice.repository.TeamProgressRepository;
import com.diplom.challengeservice.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/challenges")
@CrossOrigin(origins = "https://collabspacefrontend.onrender.com")
public class ChallengeController {

    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final XPService xpService;
    private final TaskService taskService;
    private final TeamProgressRepository teamProgressRepository;


    public ChallengeController(ChallengeRepository challengeRepository,
                               UserRepository userRepository,
                               XPService xpService, TaskService taskService, TeamProgressRepository teamProgressRepository) {
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
        this.xpService = xpService;
        this.taskService = taskService;
        this.teamProgressRepository = teamProgressRepository;
    }


    @GetMapping
    public ResponseEntity<List<ChallengeResponse>> getAllChallenges() {
        List<Challenge> challenges = challengeRepository.findAll();
        List<ChallengeResponse> response = challenges.stream()
                .map(this::convertToChallengeResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    private ChallengeResponse convertToChallengeResponse(Challenge challenge) {
        ChallengeResponse response = new ChallengeResponse();
        response.setId(challenge.getId());
        response.setTitle(challenge.getTitle());
        response.setTargetXP(challenge.getTargetXP());
        response.setStartDate(challenge.getStartDate());
        response.setEndDate(challenge.getEndDate());
        response.setCreatorId(challenge.getCreator().getId());
        response.setCreatorName(challenge.getCreator().getName());
        return response;
    }

    @PostMapping
    public ResponseEntity<ChallengeResponse> createChallenge(
            @RequestBody CreateChallengeRequest request,
            @RequestHeader("X-User-Id") Long userId) {

        if (!request.getParticipantIds().contains(userId)) {
            request.getParticipantIds().add(userId);
        }

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-User-Id header is required");
        }

        Challenge challenge = new Challenge();
        challenge.setTitle(request.getTitle());
        challenge.setTargetXP(request.getTargetXP());
        challenge.setStartDate(LocalDateTime.now());
        challenge.setEndDate(LocalDateTime.now().plusDays(request.getDurationDays()));
        challenge.setCreator(userRepository.findById(userId).orElseThrow());

        Team team = new Team();
        team.setName(request.getTeamName() != null ? request.getTeamName() : "Команда " + request.getTitle());
        team.setChallenge(challenge);

        Set<User> members = new HashSet<>();
        members.add(challenge.getCreator());

        for (Long participantId : request.getParticipantIds()) {
            members.add(userRepository.findById(participantId).orElseThrow());
        }

        team.setMembers(members);

        for (User member : members) {
            TeamProgress progress = new TeamProgress();
            progress.setTeam(team);
            progress.setUser(member);
            progress.setInitialXP(xpService.getCurrentXP(member.getId()));
            progress.setCurrentXP(progress.getInitialXP());
            team.getProgress().add(progress);
        }

        challenge.getTeams().add(team);
        challengeRepository.save(challenge);

        return ResponseEntity.ok(convertToChallengeResponse(challenge));
    }

    @GetMapping("/{challengeId}/teams/progress")
    public ResponseEntity<List<TeamProgressResponse>> getTeamsProgress(
            @PathVariable Long challengeId,
            @RequestHeader("X-User-Id") Long userId) {

        // Use the new repository method
        if (!teamProgressRepository.existsByUserIdAndChallengeId(userId, challengeId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a participant of this challenge");
        }

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));

        List<TeamProgressResponse> response = challenge.getTeams().stream()
                .flatMap(team -> team.getProgress().stream()
                        .map(progress -> {
                            int currentXP = xpService.getCurrentXP(
                                    progress.getUser().getId(),
                                    challenge.getStartDate()
                            );
                            int xpGained = currentXP - progress.getInitialXP();

                            return new TeamProgressResponse(
                                    progress.getUser().getId(),
                                    progress.getUser().getName(),
                                    progress.getUser().getPhotoPath(),
                                    team.getId(),
                                    team.getName(),
                                    progress.getInitialXP(),
                                    currentXP,
                                    xpGained
                            );
                        })
                )
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChallengeResponse> getChallengeById(@PathVariable Long id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));

        ChallengeResponse response = new ChallengeResponse(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getTargetXP(),
                challenge.getStartDate(),
                challenge.getEndDate(),
                challenge.getCreator().getId(),
                challenge.getCreator().getName()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/tasks/{id}/close")
    public ResponseEntity<Task> closeTask(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(taskService.closeTask(id, userId));
    }

    @PostMapping("/update-progress/{userId}")
    public ResponseEntity<Void> updateChallengeProgress(@PathVariable Long userId) {
        xpService.updateChallengeProgress(userId);
        return ResponseEntity.ok().build();
    }

//    @GetMapping("/user/{userId}")
//    public ResponseEntity<List<ChallengeResponse>> getChallengesByUser(@PathVariable Long userId) {
//        List<Challenge> challenges = challengeRepository.findAllByParticipantId(userId);
//        List<ChallengeResponse> response = challenges.stream()
//                .map(this::convertToChallengeResponse)
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/created-by/{userId}")
    public ResponseEntity<List<ChallengeResponse>> getChallengesCreatedByUser(@PathVariable Long userId) {
        List<Challenge> challenges = challengeRepository.findByCreatorId(userId);
        List<ChallengeResponse> response = challenges.stream()
                .map(this::convertToChallengeResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{challengeId}/is-participant")
    public ResponseEntity<Boolean> isUserParticipant(
            @PathVariable Long challengeId,
            @RequestHeader("X-User-Id") Long userId) {
        // Use either of these existing methods:
        boolean isParticipant = teamProgressRepository.existsByUserIdAndChallengeId(userId, challengeId);
        // OR
        // boolean isParticipant = teamProgressRepository.existsByUser_IdAndTeam_Challenge_Id(userId, challengeId);

        return ResponseEntity.ok(isParticipant);
    }

    @GetMapping("/user")
    public ResponseEntity<List<ChallengeResponse>> getChallengesByUser(@RequestHeader("X-User-Id") Long userId) {
        List<Challenge> challenges = challengeRepository.findAllByParticipantId(userId);
        List<ChallengeResponse> response = challenges.stream()
                .map(this::convertToChallengeResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}