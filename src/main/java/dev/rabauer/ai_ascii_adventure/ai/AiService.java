package dev.rabauer.ai_ascii_adventure.ai;

import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.LoopAgent;
import com.google.adk.models.langchain4j.LangChain4j;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Consumer;

import static dev.rabauer.ai_ascii_adventure.domain.Game.*;

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
                Task: Extract the core meaning of the following text and create a simple 3-word-title 
                for an image that you would draw for this text.
            
                Output: Only the 3 words for the title of an image. No formatting whatsoever! Just 3 words!
            
                Text:
                %s
            """;
    public final static String EXTRACT_GAME_TITLE_PROMPT = """
                Task: Extract the core meaning from the following text and create a simple
                game title and make sure the heroes name is included.
            
                Output: A maximum of 5 words for the title of a game. No formatting whatsoever! Just 5 words!
            
                Text:
                %s
            """;
    public final static String CREATE_ASCII_ART_PROMPT = """
                Input: Title of an image
                Output: Simple and recognizable ASCII art representation. Use minimal characters and clean lines.
                IMPORTANT: Only return the ASCII art. No other text or description!
                Task: Create a simple and recognizable ASCII art representation for the following title of the image
                Title of the image: %s
            """;
    private final ChatMemoryStore memoryStore;
    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;
    @Value("${ollama.model-name:llama3.2}")
    private String ollamaModelName;

    public AiService(PostgresChatMemory memoryStore) {
        this.memoryStore = memoryStore;
    }

    public AssistantWithMemory createChatModelWithMemory() {

        // Use OllamaStreamingChatModel for LangChain4J Ollama integration, with memory
        OllamaStreamingChatModel model = OllamaStreamingChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(ollamaModelName)
                .think(false)
                .timeout(Duration.ofMinutes(10))
                .build();
        AiServices<AssistantWithMemory> streamingChatModel = AiServices
                .builder(AssistantWithMemory.class)
                .streamingChatModel(model);

        // Create a chat memory provider that uses the persistent store
        ChatMemoryProvider chatMemoryProvider = memoryId ->
                MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(20)
                        .chatMemoryStore(memoryStore)
                        .build();

        streamingChatModel.chatMemoryProvider(chatMemoryProvider);

        return streamingChatModel.build();
    }

    public AssistantWithoutMemory createChatModelWithoutMemory() {

        // Use OllamaStreamingChatModel for LangChain4J Ollama integration, with memory
        OllamaStreamingChatModel model = OllamaStreamingChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(ollamaModelName)
                .think(false)
                .timeout(Duration.ofMinutes(10))
                .build();
        AiServices<AssistantWithoutMemory> streamingChatModel = AiServices
                .builder(AssistantWithoutMemory.class)
                .streamingChatModel(model);

        return streamingChatModel.build();
    }

    public AssistantWithoutMemory createChatModelWithTools(HeroUiCommunicator tools) {

        // Use OllamaStreamingChatModel for LangChain4J Ollama integration, with memory
        OllamaStreamingChatModel model = OllamaStreamingChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(ollamaModelName)
                .think(false)
                .timeout(Duration.ofMinutes(10))
                .build();
        AiServices<AssistantWithoutMemory> streamingChatModel = AiServices
                .builder(AssistantWithoutMemory.class)
                .streamingChatModel(model);
        streamingChatModel.tools(tools.getToolExecutors());

        return streamingChatModel.build();
    }

    public LlmAgents createRelevantAgents() {
        OllamaChatModel model = OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(ollamaModelName)
                .think(false)
                .timeout(Duration.ofMinutes(10))
                .build();

        LlmAgent storyAgent = LlmAgent.builder()
                .name("story_agent")
                .description("An assistant that tells the story.")
                .model(new LangChain4j(model))
                .instruction(INITIAL_STORY_PROMPT)
                .build();

        LlmAgent toolAgent = LlmAgent.builder()
                .name("tool_agent")
                .description("An assistant that can call tools in java to set some things for the player object on the ui.")
                .model(new LangChain4j(model))
                .instruction(DEFAULT_TOOL_PROMPT)
                .build();

        LlmAgent choicesAgent = LlmAgent.builder()
                .name("choices_agent")
                .description("An assistant that gives choices for the progress of the story.")
                .model(new LangChain4j(model))
                .instruction(DEFAULT_CHOICES_PROMPT)
                .build();

        LoopAgent refinementLoop = LoopAgent.builder()
                .name("CodeRefinementLoop")
                .maxIterations(5)
                .subAgents(storyAgent, choicesAgent)
                .build();

        return new LlmAgents(storyAgent, toolAgent, choicesAgent, refinementLoop);
    }

    public void generateNewStoryPart(AssistantWithMemory chatModel, long memoryId, String textPrompt,
                                     Consumer<String> onNext, Consumer<ChatResponse> onComplete) {

        ChatRequest request = ChatRequest.builder()
                .messages(UserMessage.from(textPrompt))
                .build();
        chatModel
                .chat(memoryId, request)
                .onToolExecuted((ToolExecution toolExecution) ->
                        System.out.println(toolExecution)
                )
                .onPartialResponse(onNext)
                .onCompleteResponse(onComplete)
                .onError(null)
                .start();
    }

    public void generateNewChatResponseWithoutMemory(AssistantWithoutMemory chatModel, String textPrompt,
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

    public record LlmAgents(
            LlmAgent storyAgent,
            LlmAgent toolAgent,
            LlmAgent choicesAgent,
            LoopAgent refinementLoop
    ) {
    }
}
