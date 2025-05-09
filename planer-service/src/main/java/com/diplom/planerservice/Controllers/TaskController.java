package com.diplom.planerservice.Controllers;

import com.diplom.planerservice.Model.AssigneeDto;
import com.diplom.planerservice.Model.KanbanTaskDto;
import com.diplom.planerservice.Model.Label;
import com.diplom.planerservice.Model.Task;
import com.diplom.planerservice.Repository.KanbanColumnRepository;
import com.diplom.planerservice.Repository.LabelRepository;
import com.diplom.planerservice.Repository.TaskRepository;
import com.diplom.planerservice.Service.LabelService;
import com.diplom.planerservice.Service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    private final LabelService labelService;

    private final LabelRepository labelRepository;

    private final KanbanColumnRepository kanbanColumnRepository;

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        if (task.getStatus() == null) {
            task.setStatus("Open");
        }
        return ResponseEntity.ok(taskService.saveTask(task));
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }


    @GetMapping("/task/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Long id) {
        Optional<Task> task = taskService.getTaskById(id);
        return task.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{login}")
    public ResponseEntity<List<Task>> getUserTasks(@PathVariable String login) {
        return ResponseEntity.ok(taskService.getTasksByLogin(login));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
        Optional<Task> existingTask = taskService.getTaskById(id);
        if (existingTask.isPresent()) {
            Task task = existingTask.get();

            // Обновляем только те поля, которые пришли в запросе
            if (updatedTask.getStatus() != null) {
                task.setStatus(updatedTask.getStatus());
            }
            if (updatedTask.getTitle() != null) {
                task.setTitle(updatedTask.getTitle());
            }
            if (updatedTask.getDescription() != null) {
                task.setDescription(updatedTask.getDescription());
            }
            if (updatedTask.getPriority() != null) {
                task.setPriority(updatedTask.getPriority());
            }
            if (updatedTask.getDeadline() != null) {
                task.setDeadline(updatedTask.getDeadline());
            }
            if (updatedTask.getDate() != null) {
                task.setDate(updatedTask.getDate());
            }
            if (updatedTask.getColor() != null) {
                task.setColor(updatedTask.getColor());
            }
            if (updatedTask.getLabelIds() != null) {
                task.setLabelIds(updatedTask.getLabelIds());
            }

            if (updatedTask.getAssigneeLogins() != null) {
                // Очищаем старые значения
                task.getAssigneeLogins().clear();
                task.getAssigneeNames().clear();

                // Добавляем новые
                if (!updatedTask.getAssigneeLogins().isEmpty()) {
                    task.setAssigneeLogins(updatedTask.getAssigneeLogins());
                    task.setAssigneeNames(updatedTask.getAssigneeNames());
                }
            }
            taskService.saveTask(task);
            return ResponseEntity.ok(task);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/priority")
    public ResponseEntity<Task> updateTaskPriority(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {

        Integer newPriority = request.get("priority");
        Optional<Task> existingTask = taskService.getTaskById(id);

        if (existingTask.isPresent()) {
            Task task = existingTask.get();
            task.setPriority(newPriority);
            Task savedTask = taskService.saveTask(task);
            return ResponseEntity.ok(savedTask);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/kanban/{projectId}")
    public ResponseEntity<List<KanbanTaskDto>> getKanbanTasksByProject(@PathVariable Long projectId) {
        List<Task> tasks = taskService.getKanbanTasksByProject(projectId);

        List<KanbanTaskDto> dtos = tasks.stream().map(task -> {
            KanbanTaskDto dto = new KanbanTaskDto();
            dto.setId(task.getId());
            dto.setTitle(task.getTitle());
            dto.setKanbanStatus(task.getKanbanStatus());
            dto.setPriority(task.getPriority());
            dto.setDeadline(task.getDeadline());
            dto.setColor(task.getColor());
            dto.setLabels(labelService.getLabelNamesByIds(task.getLabelIds()));

            List<AssigneeDto> assignees = new ArrayList<>();
            if (task.getAssigneeNames() != null && task.getAssigneeLogins() != null) {
                Iterator<String> nameIter = task.getAssigneeNames().iterator();
                Iterator<String> loginIter = task.getAssigneeLogins().iterator();

                while (nameIter.hasNext() && loginIter.hasNext()) {
                    assignees.add(new AssigneeDto(loginIter.next(), nameIter.next()));
                }
            }
            dto.setAssignees(assignees);

            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/kanban")
    public ResponseEntity<Task> createOrUpdateKanbanTask(@RequestBody Task task) {
        task.setIsKanban(true);
        if (task.getStatus() == null) {
            task.setStatus("Open");
        }

        if (task.getAssigneeNames() == null) {
            task.setAssigneeNames(new HashSet<>());
        }

        if (task.getDate() == null) {
            task.setDate(LocalDate.now());
        }

        return ResponseEntity.ok(taskService.saveTask(task));
    }

    @GetMapping("/user/{login}")
    public ResponseEntity<List<Task>> getUserTasksWithKanban(@PathVariable String login) {
        List<Task> tasks = taskService.getUserTasksWithKanban(login);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/by-ids")
    public ResponseEntity<List<Label>> getLabelsByIds(@RequestBody Set<Long> ids) {
        return ResponseEntity.ok(labelRepository.findByIdIn(ids));
    }

    @PatchMapping("/{id}/kanban/status")
    public ResponseEntity<Task> updateKanbanStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String kanbanStatus = request.get("kanbanStatus");

        Optional<Task> existingTask = taskService.getTaskById(id);
        if (existingTask.isPresent()) {
            Task task = existingTask.get();
            Long projectId = task.getProjectId();

            // Проверяем существует ли колонка в проекте
            boolean columnExists = kanbanColumnRepository.existsByProjectIdAndTitle(projectId, kanbanStatus);
            if (!columnExists) {
                return ResponseEntity.badRequest().build();
            }

            task.setStatus(kanbanStatus);
            task.setKanbanStatus(kanbanStatus);
            Task savedTask = taskService.saveTask(task);
            return ResponseEntity.ok(savedTask);
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String newStatus = request.get("status");
        if (newStatus == null || (!newStatus.equals("Open") && !newStatus.equals("Close"))) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Task> existingTask = taskService.getTaskById(id);
        if (existingTask.isPresent()) {
            Task task = existingTask.get();

            task.setStatus(newStatus);
            if (newStatus.equals("Close")) {
                task.setClosedAt(LocalDateTime.now());
            } else {
                task.setClosedAt(null);
            }

            Task savedTask = taskService.saveTask(task);
            return ResponseEntity.ok(savedTask);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/assignee/{assigneeLogin}")
    public ResponseEntity<List<Task>> getTasksByAssignee(@PathVariable String assigneeLogin) {
        List<Task> tasks = taskService.getTasksByAssignee(assigneeLogin);
        return ResponseEntity.ok(tasks);
    }
}

