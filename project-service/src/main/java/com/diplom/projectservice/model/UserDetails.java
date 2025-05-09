package com.diplom.projectservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDetails {
    private Long id;
    private String name;
    private String photoPath;
    private String position;
}