package com.diplom.projectservice.service;

import com.diplom.projectservice.model.Project;
import com.diplom.projectservice.repository.ProjectRepository;
import com.diplom.projectservice.repository.ProjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    public List<Project> getProjectsByCreatorId(Long creatorId) {
        return projectRepository.findByCreatorId(creatorId);
    }

    public Project saveProject(Project project) {
        return projectRepository.save(project);
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    public Project createProjectWithCreator(ProjectRequest request, Long creatorId) {
        // Ensure creator is in admin list
        if (!request.getAdminIds().contains(creatorId)) {
            request.getAdminIds().add(creatorId);
        }

        Project project = Project.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .creatorId(creatorId) // Make sure this is set
                .adminIds(request.getAdminIds())
                .teamMemberIds(request.getTeamMemberIds())
                .login(request.getLogin())
                .build();

        return projectRepository.save(project);
    }

    public Project updateProject(Long id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setAdminIds(request.getAdminIds());
        project.setTeamMemberIds(request.getTeamMemberIds());

        return projectRepository.save(project);
    }

    public void removeUserFromProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Если пользователь - создатель, полностью удаляем его из проекта
        if (project.getCreatorId().equals(userId)) {
            // Назначаем нового создателя (первого админа или null)
            Long newCreatorId = project.getAdminIds().stream()
                    .filter(id -> !id.equals(userId))
                    .findFirst()
                    .orElse(null);

            project.setCreatorId(newCreatorId);
        }

        // Удаляем из администраторов
        List<Long> newAdminIds = project.getAdminIds().stream()
                .filter(id -> !id.equals(userId))
                .collect(Collectors.toList());

        // Удаляем из участников
        List<Long> newTeamMemberIds = project.getTeamMemberIds().stream()
                .filter(id -> !id.equals(userId))
                .collect(Collectors.toList());

        project.setAdminIds(newAdminIds);
        project.setTeamMemberIds(newTeamMemberIds);

        // Если проект остался без создателя и админов - удаляем его
        if (project.getCreatorId() == null && project.getAdminIds().isEmpty()) {
            projectRepository.delete(project);
            return;
        }

        projectRepository.save(project);
    }

    public List<Project> getProjectsForUser(Long userId) {
        // Получаем проекты где пользователь админ, участник или создатель
        List<Project> userProjects = projectRepository.findProjectsByUserId(userId);
        List<Project> creatorProjects = projectRepository.findByCreatorId(userId);

        // Объединяем и фильтруем
        return Stream.concat(userProjects.stream(), creatorProjects.stream())
                .distinct()
                .filter(p -> {
                    // Проект должен иметь создателя
                    if (p.getCreatorId() == null) return false;

                    // Пользователь должен быть создателем, админом или участником
                    return p.getCreatorId().equals(userId)
                            || p.getAdminIds().contains(userId)
                            || p.getTeamMemberIds().contains(userId);
                })
                .collect(Collectors.toList());
    }

    public void addAdminToProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Если пользователь уже участник - удаляем из участников
        List<Long> newTeamMemberIds = project.getTeamMemberIds().stream()
                .filter(id -> !id.equals(userId))
                .collect(Collectors.toList());

        // Добавляем в админы, если еще не там
        List<Long> newAdminIds = new ArrayList<>(project.getAdminIds());
        if (!newAdminIds.contains(userId)) {
            newAdminIds.add(userId);
        }

        project.setAdminIds(newAdminIds);
        project.setTeamMemberIds(newTeamMemberIds);
        projectRepository.save(project);
    }

    public void addTeamMemberToProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Добавляем только если не админ и не участник
        if (!project.getAdminIds().contains(userId) &&
                !project.getTeamMemberIds().contains(userId)) {
            List<Long> newTeamMemberIds = new ArrayList<>(project.getTeamMemberIds());
            newTeamMemberIds.add(userId);
            project.setTeamMemberIds(newTeamMemberIds);
            projectRepository.save(project);
        }
    }

    public List<Project> getProjectsByUserLogin(String login) {
        // Implement logic to find projects by user login
        return projectRepository.findByLogin(login); // You'll need to add this method to the repository
    }

    public List<Project> getProjectsByLogin(String login) {
        return projectRepository.findByLogin(login);
    }

    public List<Project> getProjectsByUserId(Long userId) {
        return projectRepository.findByAdminIdsContainingOrTeamMemberIdsContaining(userId, userId);
    }


}