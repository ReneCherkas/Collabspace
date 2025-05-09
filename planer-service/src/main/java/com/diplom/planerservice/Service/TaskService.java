package com.diplom.planerservice.Service;

import com.diplom.planerservice.Model.Task;
import com.diplom.planerservice.Repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    public List<Task> getTasksByLogin(String login) {
        return taskRepository.findByLoginOrderByDateDescPriorityDesc(login);
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public List<Task> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        System.out.println("Полученные задачи: " + tasks); // Лог
        return tasks;
    }

    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public List<Task> getKanbanTasksByProject(Long projectId) {
        return taskRepository.findByProjectIdAndIsKanban(projectId, true);
    }

    public List<Task> getUserTasksWithKanban(String login) {
        return taskRepository.findByLoginOrAssigneeLoginsContains(login, login);
    }

    public List<Task> getTasksByAssignee(String assigneeLogin) {
        return taskRepository.findByAssigneeLoginsContains(assigneeLogin);
    }
}

