package com.diplom.planerservice.Controllers;

import com.diplom.planerservice.Model.KanbanColumn;
import com.diplom.planerservice.Service.KanbanColumnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kanban-columns")
@RequiredArgsConstructor
public class KanbanColumnController {
    private final KanbanColumnService kanbanColumnService;

    @PostMapping
    public ResponseEntity<KanbanColumn> createColumn(
            @RequestParam Long projectId,
            @RequestParam String title) {
        return ResponseEntity.ok(kanbanColumnService.createColumn(projectId, title));
    }

    @PutMapping("/{columnId}")
    public ResponseEntity<KanbanColumn> updateColumn(
            @PathVariable Long columnId,
            @RequestParam String newTitle) {
        return ResponseEntity.ok(kanbanColumnService.updateColumn(columnId, newTitle));
    }

    @DeleteMapping("/{columnId}")
    public ResponseEntity<Void> deleteColumn(
            @PathVariable Long columnId,
            @RequestParam Long projectId) {
        kanbanColumnService.deleteColumn(projectId, columnId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reorder/{projectId}")
    public ResponseEntity<Void> updateColumnOrder(
            @PathVariable Long projectId,
            @RequestBody List<Long> columnIdsInOrder) {
        kanbanColumnService.updateColumnPositions(projectId, columnIdsInOrder);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<KanbanColumn>> getColumnsByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(kanbanColumnService.getColumnsByProject(projectId));
    }

    @PostMapping("/default/{projectId}")
    public ResponseEntity<Void> createDefaultColumns(@PathVariable Long projectId) {
        kanbanColumnService.createDefaultColumns(projectId);
        return ResponseEntity.noContent().build();
    }
}