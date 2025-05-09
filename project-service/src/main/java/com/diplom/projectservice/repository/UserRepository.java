package com.diplom.projectservice.repository;

import com.diplom.projectservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByIdIn(List<Long> ids);
    Optional<User> findByLogin(String login);
}
