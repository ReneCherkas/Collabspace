package com.diplom.projectservice.repository;

import com.diplom.projectservice.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByCreatorId(Long creatorId);

    @Query("SELECT p FROM Project p WHERE :userId MEMBER OF p.adminIds OR :userId MEMBER OF p.teamMemberIds OR p.creatorId = :userId")
    List<Project> findProjectsByUserId(@Param("userId") Long userId);

    List<Project> findByLogin(String login);

    List<Project> findByAdminIdsContainingOrTeamMemberIdsContaining(Long adminId, Long teamMemberId);
}