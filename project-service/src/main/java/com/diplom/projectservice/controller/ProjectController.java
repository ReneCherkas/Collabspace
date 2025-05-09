package com.diplom.projectservice.controller;

import com.diplom.projectservice.repository.ProjectRequest;
import com.diplom.projectservice.service.ProjectService;
import com.diplom.projectservice.repository.UserRepository;
import com.diplom.projectservice.model.Project;
import com.diplom.projectservice.model.User;
import com.diplom.projectservice.model.UserDetails;
import com.diplom.projectservice.repository.KanbanColumnService;
import com.diplom.projectservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "https://collabspacefrontend.onrender.com")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @Autowired
    private KanbanColumnService kanbanColumnService;

    private UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProjectById(@PathVariable Long id) {
        Optional<Project> project = projectService.getProjectById(id);
        if (project.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<UserDetails> admins = userService.getUsersByIds(project.get().getAdminIds());
        List<UserDetails> teamMembers = userService.getUsersByIds(project.get().getTeamMemberIds());

        Map<String, Object> response = new HashMap<>();
        response.put("id", project.get().getId());
        response.put("title", project.get().getTitle());
        response.put("description", project.get().getDescription());
        response.put("creatorId", project.get().getCreatorId());
        response.put("admins", admins);
        response.put("teamMembers", teamMembers);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<List<Project>> getProjectsByCreatorId(@PathVariable Long creatorId) {
        return ResponseEntity.ok(projectService.getProjectsByCreatorId(creatorId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Project>> getAllProjectsAdmin() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(
            @PathVariable Long id,
            @RequestBody ProjectRequest projectRequest,
            @RequestHeader("X-User-Id") Long userId) {

        Optional<Project> existingProject = projectService.getProjectById(id);
        if (existingProject.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Проверяем, является ли пользователь администратором или создателем проекта
        if (!existingProject.get().getAdminIds().contains(userId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Project updatedProject = projectService.updateProject(id, projectRequest);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{projectId}/users/{userId}")
    public ResponseEntity<Void> removeUserFromProject(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") Long currentUserId) {

        Optional<Project> project = projectService.getProjectById(projectId);
        if (project.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Проверяем, является ли текущий пользователь администратором
        if (!project.get().getAdminIds().contains(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        projectService.removeUserFromProject(projectId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/users/{userId}")
    public ResponseEntity<Void> addUserToProject(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @RequestBody Map<String, String> request,
            @RequestHeader("X-User-Id") Long currentUserId) {

        Optional<Project> project = projectService.getProjectById(projectId);
        if (project.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Проверяем, является ли текущий пользователь администратором
        if (!project.get().getAdminIds().contains(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String role = request.get("role");
        if ("admins".equals(role)) {
            projectService.addAdminToProject(projectId, userId);
        } else if ("teamMembers".equals(role)) {
            projectService.addTeamMemberToProject(projectId, userId);
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        Project savedProject = projectService.saveProject(project);


        // Создаем стандартные колонки
        kanbanColumnService.createDefaultColumns(savedProject.getId());

        return ResponseEntity.ok(savedProject);
    }

    @GetMapping("/by-login/{login}")
    public ResponseEntity<List<Project>> getProjectsByUserLogin(@PathVariable String login) {
        try {
            // Get user by login
            User user = userService.findByLogin(login)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Get projects
            List<Project> projects = projectService.getProjectsByUserId(user.getId());
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/user")
    public ResponseEntity<List<Project>> getProjectsForCurrentUser(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(projectService.getProjectsForUser(userId));
    }

    @GetMapping("/analytics/{id}")
    public ResponseEntity<Project> getanalyticsProjectById(@PathVariable Long id) {
        Optional<Project> project = projectService.getProjectById(id);
        return project.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
