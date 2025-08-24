package dev.rabauer.ai_ascii_adventure.ai;

import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface AssistantWithoutMemory {
    TokenStream chat(@UserMessage String userMessage);
}
