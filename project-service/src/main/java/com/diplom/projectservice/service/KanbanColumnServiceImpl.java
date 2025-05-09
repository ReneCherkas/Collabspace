package com.diplom.projectservice.service;

import com.diplom.projectservice.repository.KanbanColumnService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class KanbanColumnServiceImpl implements KanbanColumnService {
    private final RestTemplate restTemplate;

    @Override
    public void createDefaultColumns(Long projectId) {
        String url = "https://planer-service.onrender.com/api/kanban-columns/default/{projectId}";
        restTemplate.postForObject(url, null, Void.class, projectId);
    }
}