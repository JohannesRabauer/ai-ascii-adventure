package dev.rabauer.ai_ascii_adventure.persistence;

import dev.rabauer.ai_ascii_adventure.domain.Game;
import dev.rabauer.ai_ascii_adventure.domain.Hero;
import dev.rabauer.ai_ascii_adventure.domain.Story;

import java.util.List;

public class GameMapper {

    public GameEntity gameToEntity(Game game) {
        GameEntity gameEntity = new GameEntity();
        HeroEntity heroEntity = new HeroEntity();
        heroEntity.setName(game.getHero().getName());
        heroEntity.setHealth(game.getHero().getHealth());
        heroEntity.setMana(game.getHero().getMana());
        heroEntity.setInventory(
                game.getHero().getInventory().stream().map(inventoryItemName -> {
                    InventoryItemEntity inventoryItemEntity = new InventoryItemEntity();
                    inventoryItemEntity.setName(inventoryItemName);
                    return inventoryItemEntity;
                }).toList()
        );
        gameEntity.setHero(heroEntity);
        gameEntity.setMemoryId(game.getMemoryId().toString());
        return gameEntity;
    }

    public Game entityToGame(GameEntity entity) {
        Hero hero = new Hero(entity.getHero().getName());
        hero.setHealth(entity.getHero().getHealth());
        hero.setMana(entity.getHero().getMana());
        entity.getHero().getInventory().stream().map(InventoryItemEntity::getName).forEach(hero::addInventory);
        return new Game(
                hero,
                //TODO: load story from database
                new Story(List.of())
        );
    }
}
