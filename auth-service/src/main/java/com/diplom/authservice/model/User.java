package com.diplom.authservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String nickname;

    @Column(unique = true, nullable = false)
    private String login;

    @Column(nullable = false)
    private String password;

    @Column
    private String role;

    @Column
    private String city;

    @Temporal(TemporalType.DATE)
    @Column(name = "birthdate")
    private java.util.Date birthdate;

    @Column(name = "photo_path")
    private String photoPath;
}
