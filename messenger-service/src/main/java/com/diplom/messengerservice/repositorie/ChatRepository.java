package com.diplom.messengerservice.repositorie;

import com.diplom.messengerservice.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByFirstUserIdOrSecondUserId(Long firstUserId, Long secondUserId);
}