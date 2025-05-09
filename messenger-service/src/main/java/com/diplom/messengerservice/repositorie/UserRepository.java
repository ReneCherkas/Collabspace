package com.diplom.messengerservice.repositorie;

import com.diplom.messengerservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.login) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchUsers(@Param("query") String query);

    List<User> findByIdIn(List<Long> ids);

    Optional<User> findByLogin(String login);
}