package com.diplom.projectservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String login;

    @ElementCollection
    @CollectionTable(name = "project_admins", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "user_id")
    private List<Long> adminIds;

    @ElementCollection
    @CollectionTable(name = "project_team_members", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "user_id")
    private List<Long> teamMemberIds;

    @Column(nullable = false)
    private Long creatorId;
}