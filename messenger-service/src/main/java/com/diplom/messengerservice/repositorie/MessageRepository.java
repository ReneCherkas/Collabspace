package com.diplom.messengerservice.repositorie;

import com.diplom.messengerservice.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatId(Long chatId);
    void deleteByChatId(Long chatId);
    List<Message> findByGroupId(Long groupId);
}
