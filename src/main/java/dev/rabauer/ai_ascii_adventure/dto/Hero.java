package dev.rabauer.ai_ascii_adventure.dto;

import java.util.ArrayList;
import java.util.List;

public class Hero {
    private final String name;
    private final List<String> inventory = new ArrayList<>();
    private Integer health;
    private Integer mana;


    public Hero(String name) {
        this.name = name;
        this.health = 100;
        this.mana = 100;
    }

    public String getName() {
        return name;
    }

    public Integer getHealth() {
        return health;
    }

    public void setHealth(Integer health) {
        this.health = health;
    }

    public Integer getMana() {
        return mana;
    }

    public void setMana(Integer mana) {
        this.mana = mana;
    }

    public List<String> getInventory() {
        return inventory;
    }

    public void addInventory(String newInventoryItem) {
        this.inventory.add(newInventoryItem);
    }

    public void clearInventory() {
        this.inventory.clear();
    }

    public void removeInventory(String inventoryItemToRemove) {
        this.inventory.remove(inventoryItemToRemove);
    }
}
