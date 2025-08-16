package dev.rabauer.ai_ascii_adventure.ai;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Consumer;

@Service
public class AiService {

    public final static String CREATE_IMAGE_PROMPT_PROMPT = """
            Given the following passage of text, extract its quintessence — the single most essential concept,
            emotion, or idea it conveys. Then, write a short, vivid prompt for generating a clear,
            minimal image that visually represents that essence. The image should be easy to understand,
            containing only the most necessary elements to express the idea, with no clutter or complex
            scenery. Avoid metaphor unless it is visually obvious. Focus on simplicity and clarity,
            suitable for both humans and AI to grasp at a glance.
            
            Text:
            %s
            """;
    public final static String EXTRACT_IMAGE_TITLE_PROMPT = """
                Read the following text, extract its core meaning, and create a simple two word
                title for an image that you would draw for this text
            
                Output: Only the two words for the title of an image.
            
                Text:
                %s
            """;
    public final static String CREATE_ASCII_ART_PROMPT = """
                Input: You are given two words for a title of an image
                Output: Simple and recognizable ASCII art representation. Use minimal characters and clean lines.
                IMPORTANT: Only return the ASCII art. No other text or description!
                Task: Create a simple and recognizable ASCII art representation for the following title of the image
                Title of the image: %s
            """;
    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;
    @Value("${ollama.model-name:llama3.2}")
    private String ollamaModelName;

    public AiService() {
        // Default constructor for Spring
    }

    public Assistant createChatModel(boolean withMemory, HeroUiCommunicator tools) {

        // Use OllamaStreamingChatModel for LangChain4J Ollama integration, with memory
        OllamaStreamingChatModel model = OllamaStreamingChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(ollamaModelName)
                .think(false)
                .timeout(Duration.ofMinutes(10))
                .build();
        AiServices<Assistant> streamingChatModel = AiServices
                .builder(Assistant.class)
                .streamingChatModel(model);

        if (tools != null) {
            streamingChatModel.tools(tools.getToolExecutors());
        }

        if (withMemory) {
            // Create a persistent chat memory store for the chat model
            InMemoryChatMemoryStore store = new InMemoryChatMemoryStore();

            // Create a chat memory provider that uses the persistent store
            ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                    .id(memoryId)
                    .maxMessages(20)
                    .chatMemoryStore(store)
                    .build();

            streamingChatModel.chatMemoryProvider(chatMemoryProvider);
        }

        return streamingChatModel.build();
    }

    public void generateNewStoryPart(Assistant chatModel, String textPrompt,
                                     Consumer<String> onNext, Consumer<ChatResponse> onComplete) {

        ChatRequest request = ChatRequest.builder()
                .messages(UserMessage.from(textPrompt))
                .build();
        chatModel
                .chat(request)
                .onToolExecuted((ToolExecution toolExecution) ->
                        System.out.println(toolExecution)
                )
                .onPartialResponse(onNext)
                .onCompleteResponse(onComplete)
                .onError(null)
                .start();
    }

    public void generateAsciiArt(Assistant chatModel, String textPrompt,
                                 java.util.function.Consumer<String> onComplete) {
        chatModel
                .chat(textPrompt)
                .onPartialResponse(response -> {
                    // Handle partial response if needed
                })
                .onCompleteResponse(completeResponse -> {
                    onComplete.accept(completeResponse.aiMessage().text());
                })
                .onError(null)
                .start();
    }
}
