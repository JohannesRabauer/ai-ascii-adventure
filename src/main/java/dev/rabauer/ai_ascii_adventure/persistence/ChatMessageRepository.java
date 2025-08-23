package dev.rabauer.ai_ascii_adventure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, String> {
    List<ChatMessageEntity> findByMemoryIdOrderByCreatedAtAsc(String memoryId);

    List<ChatMessageEntity> deleteByMemoryId(String memoryId);
}
