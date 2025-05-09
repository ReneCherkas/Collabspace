package com.diplom.messengerservice.controller;

import com.diplom.messengerservice.config.JwtTokenUtil;
import com.diplom.messengerservice.model.*;
import com.diplom.messengerservice.repositorie.ChatRepository;
import com.diplom.messengerservice.repositorie.GroupChatRepository;
import com.diplom.messengerservice.repositorie.MessageRepository;
import com.diplom.messengerservice.repositorie.UserRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "https://collabspacefrontend.onrender.com")
@RequestMapping("/api")
public class ChatController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private GroupChatRepository groupChatRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    @Transactional
    public void sendMessage(Message message) {
        System.out.println("Received message: " + message);

        // Проверяем тип чата (групповой или личный)
        if (message.getGroupId() != null) {
            System.out.println("Processing group chat message");
            boolean isParticipant = groupChatRepository.isUserParticipant(
                    message.getGroupId(),
                    message.getSenderId()
            );
            if (!isParticipant) {
                throw new RuntimeException("Sender is not a group participant");
            }

            // Сохраняем сообщение
            User sender = userRepository.findById(message.getSenderId())
                    .orElseThrow(() -> new RuntimeException("Sender not found"));
            message.setSenderName(sender.getName() != null ? sender.getName() :
                    sender.getNickname() != null ? sender.getNickname() :
                            sender.getLogin());
            message.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            Message savedMessage = messageRepository.save(message);

            // Отправляем всем участникам группы
            messagingTemplate.convertAndSend("/topic/group." + message.getGroupId(), savedMessage);

        } else if (message.getChatId() != null) {
            System.out.println("Processing private chat message");
            Chat chat = chatRepository.findById(message.getChatId())
                    .orElseThrow(() -> new RuntimeException("Chat not found"));

            if (!message.getSenderId().equals(chat.getFirstUserId()) &&
                    !message.getSenderId().equals(chat.getSecondUserId())) {
                throw new RuntimeException("Sender is not a chat participant");
            }

            // Сохраняем сообщение
            User sender = userRepository.findById(message.getSenderId())
                    .orElseThrow(() -> new RuntimeException("Sender not found"));
            message.setSenderName(sender.getName() != null ? sender.getName() :
                    sender.getNickname() != null ? sender.getNickname() :
                            sender.getLogin());
            message.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            Message savedMessage = messageRepository.save(message);

            // Отправляем обоим участникам чата через их персональные очереди
            messagingTemplate.convertAndSendToUser(
                    chat.getFirstUserId().toString(),
                    "/queue/private",
                    savedMessage
            );
            messagingTemplate.convertAndSendToUser(
                    chat.getSecondUserId().toString(),
                    "/queue/private",
                    savedMessage
            );
        } else {
            throw new RuntimeException("Neither chatId nor groupId provided");
        }
    }

    @GetMapping("/chat/messages")
    public List<MessageDTO> getMessages(
            @RequestParam(required = false) Long chatId,
            @RequestParam(required = false) Long groupId,
            @RequestParam Long currentUserId) {

        List<Message> messages;
        if (chatId != null) {
            messages = messageRepository.findByChatId(chatId);
        } else if (groupId != null) {
            messages = messageRepository.findByGroupId(groupId);
        } else {
            throw new RuntimeException("Either chatId or groupId must be provided");
        }

        return messages.stream()
                .map(msg -> {
                    MessageDTO dto = new MessageDTO();
                    dto.setId(msg.getId());
                    dto.setContent(msg.getContent());
                    dto.setTimestamp(msg.getTimestamp());
                    dto.setChatId(msg.getChatId());
                    dto.setGroupId(msg.getGroupId());
                    dto.setCurrentUser(msg.getSenderId().equals(currentUserId));
                    dto.setSenderName(msg.getSenderName());
                    dto.setSenderId(msg.getSenderId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/chat/search-users")
    public List<UserDTO> searchUsers(@RequestParam String query) {
        return userRepository.searchUsers(query.toLowerCase())
                .stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getName() != null && !user.getName().isEmpty() ?
                                user.getName() :
                                (user.getNickname() != null && !user.getNickname().isEmpty() ?
                                        user.getNickname() :
                                        user.getLogin()),
                        user.getPhotoPath()))
                .collect(Collectors.toList());
    }

    @PostMapping("/chat/create")
    public Chat createChat(@RequestBody ChatRequest chatRequest) {
        // Получаем информацию о пользователях
        User user1 = userRepository.findById(chatRequest.getUser1Id())
                .orElseThrow(() -> new RuntimeException("User1 not found"));
        User user2 = userRepository.findById(chatRequest.getUser2Id())
                .orElseThrow(() -> new RuntimeException("User2 not found"));

        Chat chat = new Chat();
        chat.setFirstUserId(user1.getId());
        chat.setSecondUserId(user2.getId());
        chat.setFirstUserName(user1.getName() != null ? user1.getName() :
                user1.getNickname() != null ? user1.getNickname() :
                        user1.getLogin());
        chat.setSecondUserName(user2.getName() != null ? user2.getName() :
                user2.getNickname() != null ? user2.getNickname() :
                        user2.getLogin());

        return chatRepository.save(chat);
    }

    @GetMapping("/chat/list")
    public List<Chat> getUserChats(@RequestParam Long userId) {
        return chatRepository.findByFirstUserIdOrSecondUserId(userId, userId);
    }

    @GetMapping("/users")
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getName() != null && !user.getName().isEmpty() ?
                                user.getName() :
                                (user.getNickname() != null && !user.getNickname().isEmpty() ?
                                        user.getNickname() :
                                        user.getLogin()),
                        user.getPhotoPath()))
                .collect(Collectors.toList());
    }

    @GetMapping("/users/by-ids")
    public List<UserDTO> getUsersByIds(@RequestParam List<Long> ids) {
        return userRepository.findByIdIn(ids).stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getName() != null ? user.getName() :
                                (user.getNickname() != null ? user.getNickname() : user.getLogin()),
                        user.getPhotoPath()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    @DeleteMapping("/chat/delete")
    public ResponseEntity<?> deleteChat(@RequestParam Long chatId) {
        try {
            Chat chat = chatRepository.findById(chatId)
                    .orElseThrow(() -> new RuntimeException("Chat not found"));

            messageRepository.deleteByChatId(chatId);

            chatRepository.delete(chat);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting chat: " + e.getMessage());
        }
    }

    @PutMapping("/chat/messages/{messageId}")
    public ResponseEntity<Message> updateMessage(
            @PathVariable Long messageId,
            @RequestBody MessageUpdateRequest updateRequest,
            @RequestHeader("Authorization") String authHeader) {

        System.out.println("Received auth header: " + authHeader); // Логируем

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("Invalid auth header format");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authHeader.substring(7);
            System.out.println("Extracted token: " + token); // Логируем токен

            Long userId = jwtTokenUtil.getUserIdFromToken(token);
            System.out.println("Extracted user ID: " + userId); // Логируем ID

            Message message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new RuntimeException("Message not found"));

            if (!message.getSenderId().equals(userId)) {
                System.out.println("User " + userId + " is not author of message " + messageId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            message.setContent(updateRequest.getContent());
            return ResponseEntity.ok(messageRepository.save(message));

        } catch (Exception e) {
            System.out.println("Auth error: " + e.getMessage()); // Логируем ошибку
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/chat/group-list")
    public List<GroupChat> getUserGroupChats(@RequestParam Long userId) {
        return groupChatRepository.findByParticipantIdsContaining(userId);
    }

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private Long getUserIdFromToken(String token) {
        try {
            return jwtTokenUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new RuntimeException("Invalid token");
        }
    }

    @PostMapping("/chat/create-group")
    public ResponseEntity<?> createGroupChat(
            @RequestBody GroupChatRequest groupChatRequest,
            @RequestHeader("Authorization") String authHeader) {

        try {
            // Проверка авторизации
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authHeader.substring(7);
            Long currentUserId = jwtTokenUtil.getUserIdFromToken(token);

            // Проверяем, что текущий пользователь включен в участники
            if (!groupChatRequest.getParticipantIds().contains(currentUserId)) {
                return ResponseEntity.badRequest().body("You must include yourself in the group");
            }

            // Проверяем, что участников достаточно для группового чата
            if (groupChatRequest.getParticipantIds().size() < 2) {
                return ResponseEntity.badRequest().body("Group chat must have at least 2 participants");
            }

            // Проверяем имя чата
            if (groupChatRequest.getName() == null || groupChatRequest.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Group chat name cannot be empty");
            }

            // Создаем чат
            GroupChat groupChat = new GroupChat();
            groupChat.setName(groupChatRequest.getName());
            groupChat.setParticipantIds(groupChatRequest.getParticipantIds());

            GroupChat savedChat = groupChatRepository.save(groupChat);
            return ResponseEntity.ok(savedChat);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating group chat: " + e.getMessage());
        }
    }

    @GetMapping("/chat/participants/{chatId}")
    public ResponseEntity<?> getChatParticipants(@PathVariable Long chatId) {
        try {
            Chat chat = chatRepository.findById(chatId)
                    .orElseThrow(() -> new RuntimeException("Chat not found"));

            List<Long> participants = Arrays.asList(chat.getFirstUserId(), chat.getSecondUserId());
            return ResponseEntity.ok(participants);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Chat not found");
        }
    }

    @MessageMapping("/chat.update")
    @Transactional
    public void updateMessageViaSocket(Message message) {
        // Validate message ID
        if (message.getId() == null) {
            throw new RuntimeException("Message ID cannot be null");
        }

        // Additional check if you want to prevent string IDs
        if (message.getId().toString().contains("-")) {
            throw new RuntimeException("Invalid message ID format");
        }

        // Get the existing message
        Message existing = messageRepository.findById(message.getId())
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Check permissions
        if (!existing.getSenderId().equals(message.getSenderId())) {
            throw new RuntimeException("User is not the author of the message");
        }

        // Update only the content, keep the original timestamp
        existing.setContent(message.getContent());
        Message updatedMessage = messageRepository.save(existing);

        // Send the updated message to the appropriate channel
        if (updatedMessage.getGroupId() != null) {
            messagingTemplate.convertAndSend("/topic/group." + updatedMessage.getGroupId(), updatedMessage);
        } else if (updatedMessage.getChatId() != null) {
            Chat chat = chatRepository.findById(updatedMessage.getChatId())
                    .orElseThrow(() -> new RuntimeException("Chat not found"));

            messagingTemplate.convertAndSendToUser(
                    chat.getFirstUserId().toString(),
                    "/queue/private",
                    updatedMessage
            );
            messagingTemplate.convertAndSendToUser(
                    chat.getSecondUserId().toString(),
                    "/queue/private",
                    updatedMessage
            );
        }
    }

    @DeleteMapping("/chat/messages/{messageId}")
    public ResponseEntity<?> deleteMessage(
            @PathVariable Long messageId,
            @RequestHeader("Authorization") String authHeader) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authHeader.substring(7);
            Long userId = jwtTokenUtil.getUserIdFromToken(token);

            Message message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new RuntimeException("Message not found"));

            if (!message.getSenderId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            messageRepository.delete(message);

            // Отправляем уведомление об удалении сообщения
            if (message.getGroupId() != null) {
                messagingTemplate.convertAndSend("/topic/group." + message.getGroupId(),
                        Map.of("action", "delete", "messageId", messageId));
            } else if (message.getChatId() != null) {
                Chat chat = chatRepository.findById(message.getChatId())
                        .orElseThrow(() -> new RuntimeException("Chat not found"));

                messagingTemplate.convertAndSendToUser(
                        chat.getFirstUserId().toString(),
                        "/queue/private",
                        Map.of("action", "delete", "messageId", messageId)
                );
                messagingTemplate.convertAndSendToUser(
                        chat.getSecondUserId().toString(),
                        "/queue/private",
                        Map.of("action", "delete", "messageId", messageId)
                );
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting message: " + e.getMessage());
        }
    }

}

