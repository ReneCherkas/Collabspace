package com.diplom.messengerservice.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDTO {
    private Long id;
    private String content;
    private String timestamp;
    private Long chatId;
    private Long groupId;
    private boolean currentUser;
    private String senderName;
    private Long senderId;
}