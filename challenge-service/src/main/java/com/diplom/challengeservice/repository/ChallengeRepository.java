package com.diplom.challengeservice.repository;


import com.diplom.challengeservice.model.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    // Найти все активные челленджи (текущая дата между startDate и endDate)
    List<Challenge> findByStartDateBeforeAndEndDateAfter(LocalDateTime now1, LocalDateTime now2);

    // Найти челленджи по создателю
    List<Challenge> findByCreatorId(Long creatorId);

    // Найти челленджи, где пользователь является участником
    @Query("SELECT DISTINCT c FROM Challenge c JOIN c.teams t JOIN t.members m WHERE m.id = :userId")
    List<Challenge> findChallengesByParticipantId(@Param("userId") Long userId);

    // Найти челленджи, которые уже завершились
    List<Challenge> findByEndDateBefore(LocalDateTime now);

    // Найти челленджи по названию (с поиском по подстроке, без учета регистра)
    List<Challenge> findByTitleContainingIgnoreCase(String titlePart);

    // Проверить, существует ли челлендж с таким названием
    boolean existsByTitle(String title);

    @Query("SELECT DISTINCT c FROM Challenge c JOIN c.teams t JOIN t.members m WHERE m.id = :userId")
    List<Challenge> findAllByParticipantId(@Param("userId") Long userId);
}