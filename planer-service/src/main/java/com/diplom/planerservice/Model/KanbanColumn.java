package com.diplom.planerservice.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kanban_columns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanbanColumn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "project_id", nullable = false)
    private Long projectId;
}