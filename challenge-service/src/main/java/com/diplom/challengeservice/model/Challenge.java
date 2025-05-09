package com.diplom.challengeservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
public class Challenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private Integer targetXP;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @ManyToOne
    @JsonIgnore
    private User creator;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL)
    private Set<Team> teams = new HashSet<>();
}
