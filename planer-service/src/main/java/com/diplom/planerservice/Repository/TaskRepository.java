package com.diplom.planerservice.Repository;

import com.diplom.planerservice.Model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByLoginOrderByDateDescPriorityDesc(String login);
    List<Task> findByProjectIdAndIsKanban(Long projectId, Boolean isKanban);
    List<Task> findByLoginOrAssigneeLoginsContains(String login, String assigneeLogin);

    long countByProjectIdAndKanbanStatus(Long projectId, String kanbanStatus);

    @Modifying
    @Query("UPDATE Task t SET t.kanbanStatus = :newStatus WHERE t.projectId = :projectId AND t.kanbanStatus = :oldStatus")
    void updateKanbanStatusByProjectIdAndOldStatus(
            @Param("projectId") Long projectId,
            @Param("oldStatus") String oldStatus,
            @Param("newStatus") String newStatus
    );

    List<Task> findByAssigneeLoginsContains(String assigneeLogin);
}
