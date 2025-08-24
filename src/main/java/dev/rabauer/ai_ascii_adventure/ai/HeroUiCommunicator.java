package dev.rabauer.ai_ascii_adventure.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.progressbar.ProgressBar;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.rabauer.ai_ascii_adventure.GameOverManager;
import dev.rabauer.ai_ascii_adventure.domain.Game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.internal.Json.fromJson;

public class HeroUiCommunicator {

    private final Game game;
    private final ProgressBar prbHealth;
    private final ProgressBar prbMana;
    private final Span spnInventory;
    private final GameOverManager gameOverManager;

    public HeroUiCommunicator(Game game, ProgressBar prbHealth, ProgressBar prbMana, Span spnInventory,
                              GameOverManager gameOverManager) {
        this.game = game;
        this.prbHealth = prbHealth;
        this.prbMana = prbMana;
        this.spnInventory = spnInventory;
        this.gameOverManager = gameOverManager;
    }

    public Map<ToolSpecification, ToolExecutor> getToolExecutors() {
        HashMap<ToolSpecification, ToolExecutor> tools = new HashMap<>();

        tools.put(
                ToolSpecification.builder()
                        .name("getHealth")
                        .description(
                                "Get the health points of the hero, ranging from 0 (dead) to 100 as integer.")
                        .build(),
                (toolExecutionRequest, memoryId) -> getHealth().toString());

        tools.put(
                ToolSpecification.builder()
                        .name("setHealth")
                        .description(
                                "Set the health points of the hero, ranging from 0 (dead) to 100 as integer.")
                        .parameters(JsonObjectSchema.builder()
                                .addIntegerProperty("health", "Health as integer from 0 to 100")
                                .build())
                        .build(),
                (toolExecutionRequest, memoryId) -> {
                    Integer health = Integer.valueOf(fromJson(toolExecutionRequest.arguments(), Map.class).get("health").toString());
                    setHealth(health);
                    return "Health set to " + health;
                });

        tools.put(
                ToolSpecification.builder()
                        .name("getMana")
                        .description("Get the mana points of the hero, ranging from 0 to 100 as integer.")
                        .build(),
                (toolExecutionRequest, memoryId) -> getMana().toString());

        tools.put(
                ToolSpecification.builder()
                        .name("setMana")
                        .description("Set the mana points of the hero, ranging from 0 to 100 as integer.")
                        .parameters(JsonObjectSchema.builder()
                                .addIntegerProperty("mana", "Mana as integer from 0 to 100")
                                .build())
                        .build(),
                (toolExecutionRequest, memoryId) -> {
                    Integer mana = Integer.valueOf(fromJson(toolExecutionRequest.arguments(), Map.class).get("mana").toString());
                    setMana(mana);
                    return "Mana set to " + mana;
                });

        tools.put(
                ToolSpecification.builder()
                        .name("getInventoryList")
                        .description("Get the full list of inventory items in the hero's inventory as list of strings.")
                        .build(),
                (toolExecutionRequest, memoryId) -> {
                    try {
                        return new ObjectMapper().writeValueAsString(getInventory());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });

        tools.put(
                ToolSpecification.builder()
                        .name("addInventoryItem")
                        .description("Add a inventory item to the hero's inventory as string.")
                        .parameters(JsonObjectSchema.builder()
                                .addStringProperty("newInventoryItem", "Item to add to the inventory as string")
                                .build())
                        .build(),
                (toolExecutionRequest, memoryId) -> {
                    String item = fromJson(toolExecutionRequest.arguments(), Map.class).get("newInventoryItem").toString();
                    addInventory(item);
                    return "Added inventory item: " + item;
                });

        tools.put(
                ToolSpecification.builder()
                        .name("clearInventory")
                        .description("Completely clear the hero's inventory. This should only happen on special occasions.")
                        .build(),
                (toolExecutionRequest, memoryId) -> {
                    clearInventory();
                    return "Inventory cleared";
                });

        tools.put(
                ToolSpecification.builder()
                        .name("removeInventory")
                        .description("Removes one inventory item from the hero's inventory as string.")
                        .parameters(JsonObjectSchema.builder()
                                .addIntegerProperty("inventoryItemToRemove", "Item to remove from the inventory as string")
                                .build())
                        .build(),
                (toolExecutionRequest, memoryId) -> {
                    String item = fromJson(toolExecutionRequest.arguments(), Map.class).get("inventoryItemToRemove").toString();
                    removeInventory(item);
                    return "Removed inventory item: " + item;
                });

        tools.put(
                ToolSpecification.builder()
                        .name("victory")
                        .description("Notifies the hero that victory was achieved.")
                        .build(),
                (toolExecutionRequest, memoryId) -> {
                    winTheGame();
                    return "Game won";
                });

        return tools;
    }

    public Integer getHealth() {
        return game.getHero().getHealth();
    }

    public void setHealth(Integer health) {
        this.game.getHero().setHealth(health);
        this.prbHealth.getUI().ifPresent(
                ui -> ui.access(() -> {
                    this.prbHealth.setMax(100);
                    this.prbHealth.setMin(0);
                    this.prbHealth.setValue(this.game.getHero().getHealth());
                }));
        checkForDeath();
    }

    public Integer getMana() {
        return game.getHero().getMana();
    }

    public void setMana(Integer mana) {
        this.game.getHero().setMana(mana);
        this.prbHealth.getUI().ifPresent(
                ui -> ui.access(() -> {
                    this.prbMana.setMax(100);
                    this.prbMana.setMin(0);
                    this.prbMana.setValue(this.game.getHero().getMana());
                }));
    }

    public List<String> getInventory() {
        return this.game.getHero().getInventory();
    }

    public void addInventory(String newInventoryItem) {
        this.game.getHero().addInventory(newInventoryItem);
        updateInventory();
    }

    public void clearInventory() {
        this.game.getHero().clearInventory();
        updateInventory();
    }

    public void removeInventory(String inventoryItemToRemove) {
        this.game.getHero().removeInventory(inventoryItemToRemove);
        updateInventory();
    }

    public void updateInventory() {
        spnInventory.getUI().ifPresent(ui -> ui.access(() ->
                spnInventory.setText(String.join(", ", this.game.getHero().getInventory()))
        ));
    }

    private void winTheGame() {
        spnInventory.getUI().ifPresent(ui -> ui.access(() ->
                this.gameOverManager.showGameOver(false)
        ));
    }

    public void checkForDeath() {
        if (this.game.getHero().getHealth() <= 0) {

            spnInventory.getUI().ifPresent(ui -> ui.access(() ->
                    this.gameOverManager.showGameOver(true)
            ));
        }
    }
}
