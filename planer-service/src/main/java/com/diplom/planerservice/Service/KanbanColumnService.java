package com.diplom.planerservice.Service;

import com.diplom.planerservice.Model.KanbanColumn;
import com.diplom.planerservice.Repository.KanbanColumnRepository;
import com.diplom.planerservice.Repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KanbanColumnService {
    private final KanbanColumnRepository kanbanColumnRepository;
    private final TaskRepository taskRepository;

    public List<KanbanColumn> getColumnsByProject(Long projectId) {
        return kanbanColumnRepository.findByProjectIdOrderByPositionAsc(projectId);
    }

    public KanbanColumn createColumn(Long projectId, String title) {
        KanbanColumn column = KanbanColumn.builder()
                .title(title)
                .projectId(projectId)
                .position(getNextPosition(projectId))
                .build();
        return kanbanColumnRepository.save(column);
    }

    @Transactional
    public KanbanColumn updateColumn(Long columnId, String newTitle) {
        KanbanColumn column = kanbanColumnRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("Column not found"));

        String oldTitle = column.getTitle(); // Сохраняем старое название

        column.setTitle(newTitle); // Обновляем название колонки

        KanbanColumn updatedColumn = kanbanColumnRepository.save(column);

        // Обновляем задачи, которые были в этой колонке
        taskRepository.updateKanbanStatusByProjectIdAndOldStatus(
                column.getProjectId(),
                oldTitle,
                newTitle
        );

        return updatedColumn;
    }

    public void updateColumnPositions(Long projectId, List<Long> columnIdsInOrder) {
        List<KanbanColumn> columns = kanbanColumnRepository.findByProjectIdOrderByPositionAsc(projectId);
        for (int i = 0; i < columnIdsInOrder.size(); i++) {
            final int position = i; // Создаем effectively final переменную
            Long columnId = columnIdsInOrder.get(i);
            Optional<KanbanColumn> columnOpt = columns.stream()
                    .filter(c -> c.getId().equals(columnId))
                    .findFirst();

            columnOpt.ifPresent(column -> {
                column.setPosition(position);
                kanbanColumnRepository.save(column);
            });
        }
    }

    private int getNextPosition(Long projectId) {
        List<KanbanColumn> columns = kanbanColumnRepository.findByProjectIdOrderByPositionAsc(projectId);
        return columns.isEmpty() ? 0 : columns.get(columns.size() - 1).getPosition() + 1;
    }

    @Transactional
    public void createDefaultColumns(Long projectId) {
        List<String> defaultColumns = Arrays.asList("Надо сделать", "В процессе", "Готово");
        int position = 1;

        for (String title : defaultColumns) {
            KanbanColumn column = KanbanColumn.builder()
                    .title(title)
                    .projectId(projectId)
                    .position(position++)
                    .build();
            kanbanColumnRepository.save(column);
        }
    }

    @Transactional
    public void deleteColumn(Long projectId, Long columnId) {
        KanbanColumn column = kanbanColumnRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("Column not found"));

        long taskCount = taskRepository.countByProjectIdAndKanbanStatus(projectId, column.getTitle());
        if (taskCount > 0) {
            throw new RuntimeException("Cannot delete column with tasks");
        }
        kanbanColumnRepository.deleteById(columnId);
    }


}