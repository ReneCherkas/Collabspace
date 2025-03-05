package com.diplom.authservice.controller;

import com.diplom.authservice.Exception.InvalidPasswordException;
import com.diplom.authservice.Exception.UserNotFoundException;
import com.diplom.authservice.config.JwtTokenProvider;
import com.diplom.authservice.model.User;
import com.diplom.authservice.model.UserProfileDTO;
import com.diplom.authservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String login = request.get("login");
        String password = request.get("password");

        if (login == null || login.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Логин не может быть пустым"));
        }
        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Пароль не может быть пустым"));
        }

        if (password.length() < 8 || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Пароль не может быть меньше 8 символов"));
        }

        try {
            User user = userService.findByLogin(login)
                    .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new InvalidPasswordException("Неверный пароль");
            }

            String token = jwtTokenProvider.generateToken(login);
            return ResponseEntity.ok(Map.of("token", token));

        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (InvalidPasswordException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Произошла внутренняя ошибка сервера"));
        }
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String login = request.get("login");
        String password = request.get("password");

        if (login == null || login.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Логин не может быть пустым"));
        }
        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Пароль не может быть пустым"));
        }

        if (login.length() < 3 || login.length() > 20) {
            return ResponseEntity.badRequest().body(Map.of("error", "Логин должен быть от 3 до 20 символов"));
        }

        if (password.length() < 8 || password.length() > 30) {
            return ResponseEntity.badRequest().body(Map.of("error", "Пароль должен быть от 8 до 30 символов"));
        }

        if (!login.matches("^[a-zA-Z0-9_@.]+$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Логин может содержать только буквы, цифры и символы _ @ ."));
        }

        if (!password.matches("^[a-zA-Z0-9!@#$%^&*()_+]+$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Пароль может содержать только буквы, цифры и символы !@#$%^&*()_+"));
        }

        if (userService.findByLogin(login).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Пользователь с таким логином уже существует"));
        }

        userService.register(login, password, "Пользователь");
        return ResponseEntity.ok(Map.of("message", "Пользователь успешно зарегистрирован!"));
    }

    @GetMapping("/users")
    public ResponseEntity<List<String>> getAllUsers() {
        List<String> users = userService.searchByLogin("");
        return ResponseEntity.ok(users);
    }

    @PostMapping("/profile/photo")
    public ResponseEntity<String> uploadPhoto(@RequestParam("photo") MultipartFile file, @RequestParam String login) {
        Optional<User> userOptional = userService.findByLogin(login);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
        }

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get("uploads", fileName);

        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            User user = userOptional.get();
            user.setPhotoPath("/uploads/" + fileName);
            userService.updateUser(user);

            return ResponseEntity.ok("Фото загружено");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка загрузки файла");
        }
    }

    @PutMapping("/profile/update")
    public ResponseEntity<String> updateProfile(@RequestParam Map<String, String> updates,
                                                @RequestParam(value = "photo", required = false) MultipartFile file) {
        String login = updates.get("login");
        Optional<User> userOptional = userService.findByLogin(login);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
        }

        User user = userOptional.get();

        updates.forEach((key, value) -> {
            switch (key) {
                case "name" -> user.setName(value);
                case "nickname" -> user.setNickname(value);
                case "password" -> user.setPassword(passwordEncoder.encode(value));
                case "city" -> user.setCity(value);
                case "birthdate" -> user.setBirthdate(java.sql.Date.valueOf(value));
            }
        });

        if (file != null && !file.isEmpty()) {
            try {
                Path uploadDir = Paths.get("uploads");
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = uploadDir.resolve(fileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                user.setPhotoPath("/uploads/" + fileName);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка загрузки файла");
            }
        }

        userService.updateUser(user);
        return ResponseEntity.ok("Профиль обновлён!");
    }



    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        System.out.println("🔍 Authentication: " + authentication);

        if (authentication == null || authentication.getPrincipal() == null) {
            System.out.println("❌ Аутентификация не установлена!");
            return ResponseEntity.status(403).body("Вы не авторизованы");
        }

        System.out.println("✅ Пользователь: " + authentication.getName());

        String login = authentication.getName();
        User user = userService.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        return ResponseEntity.ok(new UserProfileDTO(
                user.getLogin(),
                user.getRole(),
                user.getName(),
                user.getNickname(),
                user.getBirthdate() != null ? user.getBirthdate().toString() : null,
                user.getCity(),
                user.getPhotoPath()
        ));
    }
}
