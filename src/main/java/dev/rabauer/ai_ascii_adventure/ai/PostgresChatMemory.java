package dev.rabauer.ai_ascii_adventure.ai;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.rabauer.ai_ascii_adventure.persistence.ChatMessageEntity;
import dev.rabauer.ai_ascii_adventure.persistence.ChatMessageRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PostgresChatMemory implements ChatMemoryStore {

    private final ChatMessageRepository repository;

    public PostgresChatMemory(ChatMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        return repository.findByMemoryIdOrderByCreatedAtAsc(memoryId.toString())
                .stream()
                .map(entity -> ChatMessageDeserializer.messageFromJson(entity.getContent()))
                .toList();
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        messages.forEach(
                message ->
                {
                    ChatMessageEntity entity = new ChatMessageEntity();
                    entity.setMemoryId(memoryId.toString());
                    entity.setContent(ChatMessageSerializer.messageToJson(message));
                    entity.setCreatedAt(LocalDateTime.now());
                    repository.save(entity);
                }
        );
    }

    @Override
    public void deleteMessages(Object memoryId) {
        this.repository.deleteByMemoryId(memoryId.toString());
    }
}
