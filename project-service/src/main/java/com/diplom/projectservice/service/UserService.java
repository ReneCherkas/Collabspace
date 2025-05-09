package com.diplom.projectservice.service;

import com.diplom.projectservice.model.User;
import com.diplom.projectservice.model.UserDetails;
import com.diplom.projectservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<UserDetails> getUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }

        return userRepository.findByIdIn(userIds).stream()
                .map(user -> new UserDetails(
                        ((User)user).getId(),
                        ((User)user).getName(),
                        ((User)user).getPhotoPath(),
                        ((User)user).getPosition()
                ))
                .collect(Collectors.toList());
    }

    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }
}