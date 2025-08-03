package dev.rabauer.ai_ascii_adventure.ai;

import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.service.TokenStream;

public interface Assistant {

    TokenStream chat(String userMessage);

    TokenStream chat(ChatRequest userMessage);

}
