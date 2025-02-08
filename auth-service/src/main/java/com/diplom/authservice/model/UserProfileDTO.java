package com.diplom.authservice.model;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileDTO {
    private String username;
    private String role;
    private String name;
    private String nickname;
    private String birthdate;
    private String city;
    private String photo;
}
