package dev.rabauer.ai_ascii_adventure.persistence;

import dev.rabauer.ai_ascii_adventure.domain.Game;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GamePersistenceService {

    private final GameMapper gameMapper;
    private final GameRepository gameRepository;

    public GamePersistenceService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
        this.gameMapper = new GameMapper();
    }

    public void saveGameAndEnsureGameId(Game game) {
        GameEntity savedEntity = this.gameRepository.save(this.gameMapper.gameToEntity(game));
        if (game.getEntityId() == null) {
            game.setEntityId(savedEntity.getId());
        }
    }

    public List<Game> loadGames() {
        return this.gameRepository.findAll().stream().map(this.gameMapper::entityToGame).toList();
    }

    public Optional<Game> loadGame(String gameId) {
        return this.gameRepository.findById(Long.parseLong(gameId)).map(this.gameMapper::entityToGame);
    }
}
