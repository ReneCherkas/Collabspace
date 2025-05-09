package com.diplom.planerservice.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String login;

    @Column(nullable = false)
    private String title;

    private String description;
    private LocalDateTime deadline;
    private LocalDate date;
    private String color;

    @Column(nullable = false)
    private Integer priority = 0;

    private String label;

    @Column(name = "kanban_status")
    private String kanbanStatus;

    private String status;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    private String team;

    @Column(name = "assignee_id")
    private Long assigneeId;

    @ElementCollection
    @CollectionTable(name = "task_labels", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "label_id")
    private Set<Long> labelIds;

    @Column(name = "is_kanban")
    private Boolean isKanban = false;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "assignee_logins")
    @ElementCollection
    @CollectionTable(name = "task_assignees", joinColumns = @JoinColumn(name = "task_id"))
    private Set<String> assigneeLogins = new HashSet<>();

    @Column(name = "assignee_names")
    @ElementCollection
    @CollectionTable(name = "task_assignees_names", joinColumns = @JoinColumn(name = "task_id"))
    private Set<String> assigneeNames = new HashSet<>();
}