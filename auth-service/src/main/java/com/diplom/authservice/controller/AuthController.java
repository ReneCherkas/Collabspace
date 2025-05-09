package com.diplom.authservice.controller;

import com.diplom.authservice.Exception.InvalidPasswordException;
import com.diplom.authservice.Exception.UserNotFoundException;
import com.diplom.authservice.config.JwtTokenProvider;
import com.diplom.authservice.model.User;
import com.diplom.authservice.model.UserProfileDTO;
import com.diplom.authservice.repository.UserRepository;
import com.diplom.authservice.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String login = request.get("login");
        String password = request.get("password");

        try {
            Optional<User> userOpt = userService.findByLogin(login);

            if (userOpt.isEmpty()) {
                userOpt = userService.findByNickname("@" + login);
            }

            User user = userOpt.orElseThrow(() -> {
                return new UserNotFoundException("Неверный логин или пароль");
            });

            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new InvalidPasswordException("Неверный логин или пароль");
            }

            String token = jwtTokenProvider.generateToken(user.getLogin());
            return ResponseEntity.ok(Map.of("token", token, "user", user));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Неверный логин или пароль"));
        }
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String login = request.get("login");
        String password = request.get("password");
        String name = request.get("name");

        if (login == null || login.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Логин не может быть пустым"));
        }
        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Пароль не может быть пустым"));
        }
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Имя не может быть пустым"));
        }

        if (login.length() < 3 || login.length() > 20) {
            return ResponseEntity.badRequest().body(Map.of("error", "Логин должен быть от 3 до 20 символов"));
        }
        if (!login.matches("^[a-zA-Z0-9_@.]{3,20}$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Логин может содержать только буквы, цифры и символы _ @ ."));
        }
        if (userService.findByLogin(login).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Пользователь с таким логином уже существует"));
        }

        if (password.length() < 8 || password.length() > 30) {
            return ResponseEntity.badRequest().body(Map.of("error", "Пароль должен быть от 8 до 30 символов"));
        }
        if (!password.matches("^[a-zA-Z0-9!@#$%^&*()_+]+$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Пароль может содержать только буквы, цифры и символы !@#$%^&*()_+"));
        }

        if (!name.matches("^[А-Яа-яA-Za-z\\s-]{2,50}$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Имя должно содержать только буквы, пробелы и дефисы, от 2 до 50 символов"));
        }

        String nickname = generateUniqueNickname();
        userService.register(login, password, "Пользователь", nickname, name);
        return ResponseEntity.ok(Map.of("message", "Пользователь успешно зарегистрирован!"));
    }


    private String generateUniqueNickname() {
        String baseNickname = "@" + UUID.randomUUID().toString().substring(0, 8);
        while (userService.findByNickname(baseNickname).isPresent()) {
            baseNickname = "@" + UUID.randomUUID().toString().substring(0, 8);
        }
        return baseNickname;
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
            user.setPhotoPath(fileName);
            userService.updateUser(user);

            return ResponseEntity.ok("Фото загружено");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка загрузки файла");
        }
    }

    @PutMapping(value = "/profile/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(
            @RequestPart("updates") String updatesJson,
            @RequestPart(value = "photo", required = false) MultipartFile file,
            Authentication authentication) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> updates = objectMapper.readValue(updatesJson, new TypeReference<Map<String, String>>() {});

            String currentLogin = authentication.getName();
            Optional<User> userOptional = userService.findByLogin(currentLogin);

            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Пользователь не найден"));
            }

            User user = userOptional.get();
            boolean loginChanged = false;
            String newLogin = null;

            if (updates.containsKey("newLogin")) {
                newLogin = updates.get("newLogin");

                if (newLogin.length() < 3 || newLogin.length() > 20) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Логин должен быть от 3 до 20 символов"));
                }

                if (!newLogin.equals(user.getLogin()) &&
                        userService.findByLogin(newLogin).isPresent()) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Map.of("error", "Этот логин уже занят"));
                }

                user.setLogin(newLogin);
                loginChanged = true;
            }

            if (updates.containsKey("name")) {
                user.setName(updates.get("name"));
            }

            if (updates.containsKey("nickname")) {
                user.setNickname(updates.get("nickname"));
            }

            if (updates.containsKey("password") && !updates.get("password").isEmpty()) {
                if (!passwordEncoder.matches(updates.get("password"), user.getPassword())) {
                    user.setPassword(passwordEncoder.encode(updates.get("password")));
                }
            }

            if (file != null && !file.isEmpty()) {
                if (file.getSize() > 2 * 1024 * 1024) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Размер файла не должен превышать 2MB"));
                }

                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path uploadDir = Paths.get("uploads");

                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                Path filePath = uploadDir.resolve(fileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                user.setPhotoPath("/uploads/" + fileName);
            }

            userService.updateUser(user);

            if (loginChanged) {
                String newToken = jwtTokenProvider.generateToken(newLogin);
                return ResponseEntity.ok(Map.of(
                        "message", "Профиль успешно обновлен",
                        "newToken", newToken,
                        "newLogin", newLogin
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Профиль успешно обновлен"
            ));

        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Неверный формат данных"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при обработке файла"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Внутренняя ошибка сервера"));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {

        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(403).body("Вы не авторизованы");
        }

        String login = authentication.getName();
        User user = userService.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        return ResponseEntity.ok(new UserProfileDTO(
                user.getLogin(),
                user.getRole(),
                user.getName(),
                user.getNickname(),
                user.getPhotoPath()
        ));
    }

    @Value("${file.access-url}")
    private String fileAccessUrl;

    @GetMapping("/user-profile/{userId}")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String photoUrl = user.getPhotoPath() != null
                ? fileAccessUrl + user.getPhotoPath()
                : fileAccessUrl + "/default-avatar.jpg";

        return ResponseEntity.ok(new UserProfileDTO(
                user.getLogin(),
                user.getRole(),
                user.getName(),
                user.getNickname(),
                photoUrl
        ));
    }

    @PostMapping("/check-password")
    public ResponseEntity<?> checkPassword(@RequestBody Map<String, String> request) {
        String rawPassword = request.get("password");
        String encodedPassword = request.get("encodedPassword");

        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);

        return ResponseEntity.ok(Map.of(
                "matches", matches,
                "rawPassword", rawPassword,
                "encodedPassword", encodedPassword
        ));
    }

    @GetMapping("/generate-hash")
    public ResponseEntity<String> generateHash(@RequestParam String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        return ResponseEntity.ok(encodedPassword);
    }

    @GetMapping("/by-login/{login}")
    public ResponseEntity<User> getUserByLogin(@PathVariable String login) {
        Optional<User> user = userRepository.findByLogin(login);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
