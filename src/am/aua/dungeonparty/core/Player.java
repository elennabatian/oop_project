package am.aua.dungeonparty.core;

import am.aua.dungeonparty.inventory.Inventory;
import am.aua.dungeonparty.utils.GameConstants;
import java.util.ArrayList;
import java.util.List;

// Abstract base class for all heroes in DungeonParty.
// Defines common stats, inventory, skill list, and coin management.
// Subclasses must implement their unique combat behaviour.

public abstract class Player {
    protected String name;
    protected int health;
    protected int mana;
    protected int attackPower;
    protected int coins;
    protected Inventory inventory;
    protected List<Skill> skills;
    protected boolean reviveUsed;   // used by Healer subclass

    public Player(String name, int health, int mana, int attackPower) {
        this.name = name;
        this.health = health;
        this.mana = mana;
        this.attackPower = attackPower;
        this.coins = GameConstants.STARTING_COINS;
        this.inventory = new Inventory();
        this.skills = new ArrayList<>();
        this.reviveUsed = false;
    }

    // Perform a basic attack on the target. Override a anum subclassnerum bonusneri hamar
    public TurnResult attack(Player target) {
        int damage = attackPower;
        target.takeDamage(damage);
        return new TurnResult(true, name + " attacks for " + damage + " damage.", damage, 0);
    }

    // Apply incoming damage. Rogue-i depqum qich
    public void takeDamage(int amount) {
        health = Math.max(0, health - amount);
    }

    public boolean isAlive() {
        return health > 0;
    }

    public int getCoins() {
        return coins;
    }

    public void addCoins(int amount) {
        this.coins += amount;
    }

    public void spendCoins(int amount) throws NotEnoughCoinsException {
        if (coins < amount) throw new NotEnoughCoinsException(name + " has insufficient coins!");
        coins -= amount;
    }

    public Inventory getInventory() { return inventory; }
    public List<Skill> getSkills() { return skills; }
    public String getName() { return name; }
    public int getHealth() { return health; }
    public int getMana() { return mana; }
    public void setHealth(int health) { this.health = health; }
    public void setMana(int mana) { this.mana = mana; }
    public boolean hasReviveUsed() { return reviveUsed; }
    public void setReviveUsed(boolean used) { this.reviveUsed = used; }

    public static class NotEnoughCoinsException extends Exception {
        public NotEnoughCoinsException(String message) { super(message); }
    }
}