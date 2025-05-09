package com.diplom.messengerservice.repositorie;

import com.diplom.messengerservice.model.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {
    List<GroupChat> findByParticipantIdsContaining(Long userId);

    @Query("SELECT COUNT(g) > 0 FROM GroupChat g JOIN g.participantIds p WHERE g.id = :groupId AND p = :userId")
    boolean isUserParticipant(@Param("groupId") Long groupId, @Param("userId") Long userId);
}