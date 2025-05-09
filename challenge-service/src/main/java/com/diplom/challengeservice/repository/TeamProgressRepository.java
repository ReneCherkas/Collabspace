package com.diplom.challengeservice.repository;


import com.diplom.challengeservice.model.TeamProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamProgressRepository extends JpaRepository<TeamProgress, Long> {

    @Query("SELECT tp FROM TeamProgress tp WHERE tp.user.id = :userId " +
            "AND tp.team.challenge.endDate > CURRENT_TIMESTAMP")
    List<TeamProgress> findActiveByUserId(@Param("userId") Long userId);

    // Ищем прогресс по ID пользователя
    @Query("SELECT tp FROM TeamProgress tp WHERE tp.user.id = :userId")
    List<TeamProgress> findByUserId(@Param("userId") Long userId);

    // Или альтернативный вариант с join:
    @Query("SELECT tp FROM TeamProgress tp JOIN tp.user u WHERE u.id = :userId")
    List<TeamProgress> findByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(tp) > 0 FROM TeamProgress tp WHERE tp.user.id = :userId AND tp.team.challenge.id = :challengeId")
    boolean existsByUserIdAndChallengeId(@Param("userId") Long userId, @Param("challengeId") Long challengeId);

    // Alternative method name (Spring Data JPA will implement it automatically)
    boolean existsByUser_IdAndTeam_Challenge_Id(Long userId, Long challengeId);
}