package dev.rabauer.ai_ascii_adventure.persistence;

import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class HeroEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    @OneToMany(cascade = CascadeType.ALL)
    @Column(name = "item")
    private List<InventoryItemEntity> inventory = new ArrayList<>();
    private Integer health;
    private Integer mana;
    // JPA requires a no-args constructor with at least protected visibility
    protected HeroEntity() {
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<InventoryItemEntity> getInventory() {
        return inventory;
    }

    public void setInventory(List<InventoryItemEntity> inventory) {
        this.inventory = inventory;
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
}
