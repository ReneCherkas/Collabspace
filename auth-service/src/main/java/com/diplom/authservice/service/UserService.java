package com.diplom.authservice.service;

import com.diplom.authservice.model.User;
import com.diplom.authservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Регистрация пользователя
    public User register(String username, String password, String role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Пользователь с таким именем уже существует!");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // Хеширование пароля
        user.setRole(role);
        return userRepository.save(user); // Сохранение в базе данных
    }

    // Проверка логина пользователя
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<String> searchByUsername(String query) {
        return userRepository.findByUsernameContainingIgnoreCase(query)
                .stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
    }

}

