package com.diplom.planerservice.Model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class KanbanTaskDto {
    private Long id;
    private String title;
    private String kanbanStatus;
    private Integer priority;
    private LocalDateTime deadline;
    private String color;
    private List<String> labels;
    private List<AssigneeDto> assignees;
}