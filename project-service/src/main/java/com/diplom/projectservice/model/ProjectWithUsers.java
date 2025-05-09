package com.diplom.projectservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProjectWithUsers {
    private Project project;
    private List<UserDetails> admins;
    private List<UserDetails> teamMembers;
}