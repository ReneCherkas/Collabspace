package com.diplom.planerservice.Repository;

import com.diplom.planerservice.Model.KanbanColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KanbanColumnRepository extends JpaRepository<KanbanColumn, Long> {
    List<KanbanColumn> findByProjectIdOrderByPositionAsc(Long projectId);
    boolean existsByProjectIdAndTitle(Long projectId, String title);
    void deleteById(Long columnId);
}