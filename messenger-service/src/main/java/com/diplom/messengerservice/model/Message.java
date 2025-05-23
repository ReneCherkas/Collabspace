package com.diplom.messengerservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    private String timestamp;
    private Long senderId;
    private String senderName;
    private Long chatId;
    private Long groupId;
}