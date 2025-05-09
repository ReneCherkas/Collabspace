package com.diplom.challengeservice.repository;

import com.diplom.challengeservice.model.Task;
import com.diplom.challengeservice.model.TeamProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssigneeIdAndStatus(Long assigneeId, String status);
    List<Task> findByStatus(String status);

    @Query("SELECT t FROM Task t JOIN t.assigneeLogins al WHERE al = :login AND t.status = :status")
    List<Task> findByAssigneeLoginAndStatus(@Param("login") String login, @Param("status") String status);

    @Query("SELECT t FROM Task t WHERE " +
            "(t.assigneeId = :userId OR :userLogin MEMBER OF t.assigneeLogins) " +
            "AND t.status = 'Close'")
    List<Task> findByAssigneeIdOrAssigneeLoginsContainingAndStatus(
            @Param("userId") Long userId,
            @Param("userLogin") String userLogin,
            @Param("status") String status);

    @Query("SELECT t FROM Task t WHERE t.status = 'Close' AND " +
            "(t.assigneeId = :userId OR :userLogin MEMBER OF t.assigneeLogins OR t.login = :userLogin)")
    List<Task> findByStatusAndAssigneeIdOrAssigneeLoginsContainingOrLogin(
            @Param("status") String status,
            @Param("userId") Long userId,
            @Param("userLogin") String userLogin
    );

    @Query("SELECT t FROM Task t WHERE t.status = :status " +
            "AND (t.assigneeId = :userId OR :userLogin MEMBER OF t.assigneeLogins OR t.login = :userLogin)")
    List<Task> findByStatusAndAssignee(
            @Param("status") String status,
            @Param("userId") Long userId,
            @Param("userLogin") String userLogin
    );

    @Query("SELECT t FROM Task t WHERE t.status = :status " +
            "AND t.closedAt >= :sinceDate " +
            "AND (t.assigneeId = :userId OR :userLogin MEMBER OF t.assigneeLogins OR t.login = :userLogin)")
    List<Task> findByStatusAndClosedAtAfterAndAssignee(
            @Param("status") String status,
            @Param("sinceDate") LocalDateTime sinceDate,
            @Param("userId") Long userId,
            @Param("userLogin") String userLogin
    );

    @Query("SELECT tp FROM TeamProgress tp WHERE tp.user.id = :userId AND tp.team.challenge.id = :challengeId")
    Optional<TeamProgress> findByUserIdAndChallengeId(@Param("userId") Long userId, @Param("challengeId") Long challengeId);

    // Проверить, является ли пользователь участником челленджа
    @Query("SELECT COUNT(tp) > 0 FROM TeamProgress tp WHERE tp.user.id = :userId AND tp.team.challenge.id = :challengeId")
    boolean isUserParticipant(@Param("userId") Long userId, @Param("challengeId") Long challengeId);
}



