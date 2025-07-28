package dev.rabauer.ai_ascii_adventure.ai;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.progressbar.ProgressBar;
import dev.rabauer.ai_ascii_adventure.GameManager;
import dev.rabauer.ai_ascii_adventure.dto.Hero;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;

public class HeroUiCommunicator {

    private final Hero hero;
    private final ProgressBar prbHealth;
    private final ProgressBar prbMana;
    private final Span spnInventory;
    private final GameManager gameManager;


    public HeroUiCommunicator(Hero hero, ProgressBar prbHealth, ProgressBar prbMana, Span spnInventory, GameManager gameManager) {
        this.hero = hero;
        this.prbHealth = prbHealth;
        this.prbMana = prbMana;
        this.spnInventory = spnInventory;
        this.gameManager = gameManager;
    }

    @Tool(description = "Get the maximum health points of the hero as integer.")
    public Integer getMaxHealth() {
        return this.hero.getMaxHealth();
    }

    @Tool(description = "Get the health points of the hero, ranging from 0 (dead) to max health as integer.")
    public Integer getHealth() {
        return hero.getHealth();
    }

    @Tool(description = "Set the health points of the hero, ranging from 0 (dead) to max health as integer.")
    public void setHealth(Integer health) {
        this.hero.setHealth(health);
        this.prbHealth.getUI().ifPresent(
                ui -> ui.access(() -> {
                    this.prbHealth.setMax(this.hero.getMaxHealth());
                    this.prbHealth.setMin(0);
                    this.prbHealth.setValue(this.hero.getHealth());
                })
        );
        checkForDeath();
    }

    @Tool(description = "Get the maximum mana points of the hero as integer.")
    public Integer getMaxMana() {
        return this.hero.getMaxMana();
    }

    @Tool(description = "Get the mana points of the hero, ranging from 0 to max mana as integer.")
    public Integer getMana() {
        return hero.getMana();
    }

    @Tool(description = "Set the mana points of the hero, ranging from 0 to max mana as integer.")
    public void setMana(Integer mana) {
        this.hero.setMana(mana);
        this.prbHealth.getUI().ifPresent(
                ui -> ui.access(() -> {
                    this.prbMana.setMax(this.hero.getMaxMana());
                    this.prbMana.setMin(0);
                    this.prbMana.setValue(this.hero.getMana());
                })
        );
    }

    @Tool(description = "Get the full list of inventory items in the hero's inventory as list of strings.")
    public List<String> getInventory() {
        return this.hero.getInventory();
    }

    @Tool(description = "Add one inventory item to the hero's inventory as string.")
    public void addInventory(String newInventoryItem) {
        this.hero.addInventory(newInventoryItem);
        updateInventory();
    }

    @Tool(description = "Completely clear the hero's inventory.")
    public void clearInventory() {
        this.hero.clearInventory();
        updateInventory();
    }

    @Tool(description = "Removes one inventory item from the hero's inventory as string.")
    public void removeInventory(String inventoryItemToRemove) {
        this.hero.removeInventory(inventoryItemToRemove);
        updateInventory();
    }

    public void updateInventory() {
        spnInventory.getUI().ifPresent(ui -> ui.access(() ->
        {
            spnInventory.setText(String.join(", ", this.hero.getInventory()));
        }));
    }


    @Tool(description = "The hero succeeded in this game.")
    private void winTheGame() {
        this.gameManager.showGameOver(false);
    }

    public void checkForDeath() {
        if (this.hero.getHealth() <= 0) {
            this.gameManager.showGameOver(true);
        }
    }
}
