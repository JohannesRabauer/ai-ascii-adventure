package dev.rabauer.ai_ascii_adventure.persistence;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageType;
import dev.rabauer.ai_ascii_adventure.domain.Game;
import dev.rabauer.ai_ascii_adventure.domain.StoryPart;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GamePersistenceService {

    private final GameMapper gameMapper;
    private final GameRepository gameRepository;
    private final ChatMessageRepository chatMessageRepository;

    public GamePersistenceService(GameRepository gameRepository, ChatMessageRepository chatMessageRepository) {
        this.gameRepository = gameRepository;
        this.gameMapper = new GameMapper();
        this.chatMessageRepository = chatMessageRepository;
    }

    public void saveGameAndEnsureGameId(Game game) {
        GameEntity savedEntity = this.gameRepository.save(this.gameMapper.gameToEntity(game));
        if (game.getEntityId() == null) {
            game.setEntityId(savedEntity.getId());
        }
    }

    public List<Game> loadGamesWithoutStory() {
        return this.gameRepository.findAll().stream().map(this.gameMapper::entityToGameWithoutStory).toList();
    }

    public Optional<Game> loadGame(String gameId) {
        Optional<Game> foundGame = this.gameRepository.findById(Long.parseLong(gameId)).map(this.gameMapper::entityToGameWithoutStory);
        foundGame
                .ifPresent(game ->
                        this.chatMessageRepository
                                .findByMemoryIdOrderByCreatedAtAsc(gameId)
                                .stream()
                                .map(entity -> ChatMessageDeserializer.messageFromJson(entity.getContent()))
                                .filter(message -> message.type().equals(ChatMessageType.AI))
                                .forEach(chatMessageEntity ->
                                        game
                                                .getStory()
                                                .storyParts()
                                                .add(new StoryPart(((AiMessage) chatMessageEntity).text()))
                                )
                );
        return foundGame;
    }
}
