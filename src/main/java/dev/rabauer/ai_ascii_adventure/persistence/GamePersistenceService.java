package dev.rabauer.ai_ascii_adventure.persistence;

import dev.rabauer.ai_ascii_adventure.domain.Game;
import org.springframework.stereotype.Service;

@Service
public class GamePersistenceService {

    private final GameMapper gameMapper;
    private final GameRepository gameRepository;

    public GamePersistenceService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
        this.gameMapper = new GameMapper();
    }

    public void saveGame(Game game) {
        this.gameRepository.save(this.gameMapper.gameToEntity(game));
    }
}
