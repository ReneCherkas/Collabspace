package com.diplom.messengerservice.controller;

import com.diplom.messengerservice.model.Message;
import com.diplom.messengerservice.repositorie.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChatController {

    @Autowired
    private MessageRepository messageRepository;

    @MessageMapping("/chat.send")
    @SendTo("/topic/messages")
    public Message sendMessage(Message message){
        messageRepository.save(message);
        return message;
    }

    @GetMapping("/chat/messages")
    public List<Message> getAllMessages(@RequestParam String chatId) {
        return messageRepository.findByChatId(chatId);
    }
}
