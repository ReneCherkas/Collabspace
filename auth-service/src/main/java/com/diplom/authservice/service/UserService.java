package com.diplom.authservice.service;

import com.diplom.authservice.model.User;
import com.diplom.authservice.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<String> searchByLogin(String query) {
        return userRepository.findByLoginContainingIgnoreCase(query)
                .stream()
                .map(User::getLogin)
                .collect(Collectors.toList());
    }

    public void register(String login, String password, String role, String nickname, String name) {
        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setNickname(nickname);
        user.setName(name);

        userRepository.save(user);
    }

    public Optional<User> findByNickname(String nickname) {  // Новый метод
        return userRepository.findByNickname(nickname);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        return org.springframework.security.core.userdetails.User.withUsername(user.getLogin())
                .password(user.getPassword())
                .build();
    }

    public User register(String login, String password, String role) {
        if (userRepository.findByLogin(login).isPresent()) {
            throw new RuntimeException("Пользователь с таким логином уже существует!");
        }
        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        return userRepository.save(user);
    }

    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }


    public void updateUser(User updatedUser) {
        User existingUser = userRepository.findByLogin(updatedUser.getLogin())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (updatedUser.getName() != null && !updatedUser.getName().isEmpty()) {
            existingUser.setName(updatedUser.getName());
        }

        if (updatedUser.getNickname() != null && !updatedUser.getNickname().isEmpty()) {
            existingUser.setNickname(updatedUser.getNickname());
        }

        if (updatedUser.getPassword() != null &&
                !updatedUser.getPassword().isEmpty() &&
                !updatedUser.getPassword().equals(existingUser.getPassword())) {
            existingUser.setPassword(updatedUser.getPassword());
        }

        if (updatedUser.getPhotoPath() != null) {
            existingUser.setPhotoPath(updatedUser.getPhotoPath());
        }

        userRepository.save(existingUser);
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

}
