package com.diplom.messengerservice.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChatRequest {
    private Long user1Id;
    private Long user2Id;
}