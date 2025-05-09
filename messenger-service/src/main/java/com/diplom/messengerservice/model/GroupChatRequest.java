package com.diplom.messengerservice.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class GroupChatRequest {
    private String name;
    private List<Long> participantIds;
}