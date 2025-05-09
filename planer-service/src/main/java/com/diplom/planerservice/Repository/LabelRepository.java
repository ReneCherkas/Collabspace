package com.diplom.planerservice.Repository;

import com.diplom.planerservice.Model.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {
    List<Label> findByLogin(String login);
    List<Label> findByIdIn(Set<Long> ids);
}