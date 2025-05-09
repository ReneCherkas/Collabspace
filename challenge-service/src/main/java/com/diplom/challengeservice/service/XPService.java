package com.diplom.challengeservice.service;


import com.diplom.challengeservice.model.Challenge;
import com.diplom.challengeservice.model.Task;
import com.diplom.challengeservice.model.TeamProgress;
import com.diplom.challengeservice.model.User;
import com.diplom.challengeservice.repository.TaskRepository;
import com.diplom.challengeservice.repository.TeamProgressRepository;
import com.diplom.challengeservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class XPService {
    private final TeamProgressRepository teamProgressRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public XPService(TeamProgressRepository teamProgressRepository,
                     TaskRepository taskRepository, UserRepository userRepository) {
        this.teamProgressRepository = teamProgressRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public Integer getCurrentXP(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        String userLogin = user.getLogin();

        List<Task> completedTasks = taskRepository.findByStatusAndAssignee(
                "Close",
                userId,
                userLogin
        );

        return calculateTasksXP(completedTasks) + calculateQuestsXP(completedTasks);
    }

    public Integer getCurrentXP(Long userId, LocalDateTime sinceDate) {
        User user = userRepository.findById(userId).orElseThrow();
        String userLogin = user.getLogin();

        List<Task> completedTasks = taskRepository.findByStatusAndClosedAtAfterAndAssignee(
                "Close",
                sinceDate,
                userId,
                userLogin
        );

        return calculateTasksXP(completedTasks) + calculateQuestsXP(completedTasks);
    }

    private int calculateTasksXP(List<Task> completedTasks) {
        int totalXP = 0;

        for (Task task : completedTasks) {
            if (task.getDeadline() != null && task.getClosedAt() != null) {
                if (task.getClosedAt().isBefore(task.getDeadline())) {
                    totalXP += 20; // Выполнено до дедлайна
                } else {
                    totalXP += 10; // Просрочено
                }
            } else {
                totalXP += 15; // Без дедлайна
            }
        }
        return totalXP;
    }

    private int calculateQuestsXP(List<Task> completedTasks) {
        int questsXP = 0;

        // Ежедневный квест: выполнить 3 задачи сегодня
        long todayTasks = completedTasks.stream()
                .filter(task -> task.getClosedAt() != null &&
                        task.getClosedAt().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                .count();
        if (todayTasks >= 3) {
            questsXP += 30;
        }

        // Срочные задачи: выполнить 3 срочные задачи из канбана
        long urgentKanbanTasks = completedTasks.stream()
                .filter(task -> "urgent".equals(task.getPriority()) &&
                        Boolean.TRUE.equals(task.getIsKanban()))
                .count();
        if (urgentKanbanTasks >= 3) {
            questsXP += 50;
        }

        // Канбан-мастер: выполнить 5 задач из канбан-доски
        long kanbanTasks = completedTasks.stream()
                .filter(task -> Boolean.TRUE.equals(task.getIsKanban()))
                .count();
        if (kanbanTasks >= 5) {
            questsXP += 70;
        }

        return questsXP;
    }

    public void updateChallengeProgress(Long userId) {
        List<TeamProgress> progresses = teamProgressRepository.findActiveByUserId(userId);

        for (TeamProgress progress : progresses) {
            Challenge challenge = progress.getTeam().getChallenge();

            // Получаем текущий XP с учетом даты начала челленджа
            int currentXP = getCurrentXP(userId, challenge.getStartDate());

            // Рассчитываем XP, заработанный именно в этом челлендже
            int xpGained = currentXP - progress.getInitialXP();

            // Обновляем прогресс
            progress.setCurrentXP(currentXP);
            teamProgressRepository.save(progress);

            System.out.printf("Updated progress for user %d in challenge %s: " +
                            "initialXP=%d, currentXP=%d, xpGained=%d%n",
                    userId, challenge.getTitle(),
                    progress.getInitialXP(), currentXP, xpGained);
        }
    }
}