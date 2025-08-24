package dev.rabauer.ai_ascii_adventure.ai;

import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface AssistantWithMemory {

    TokenStream chat(@MemoryId long memoryId, @UserMessage String userMessage);

    TokenStream chat(@MemoryId long memoryId, @UserMessage ChatRequest userMessage);

}
