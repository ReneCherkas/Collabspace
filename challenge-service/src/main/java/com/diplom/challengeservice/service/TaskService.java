package com.diplom.challengeservice.service;

import com.diplom.challengeservice.model.Task;
import com.diplom.challengeservice.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final XPService xpService;

    public Task closeTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId).orElseThrow();
        task.setStatus("Close");
        task.setClosedAt(LocalDateTime.now());

        // Если задача из канбана, убедитесь что assigneeId установлен
        if (task.getIsKanban() && task.getAssigneeId() == null) {
            task.setAssigneeId(userId);
        }

        Task savedTask = taskRepository.save(task);
        xpService.updateChallengeProgress(userId);
        return savedTask;
    }
}