package am.aua.dungeonparty.core;

import am.aua.dungeonparty.inventory.Inventory;
import am.aua.dungeonparty.utils.GameConstants;
import am.aua.dungeonparty.exceptions.NotEnoughCoinsException;   // added import
import java.util.ArrayList;
import java.util.List;

public abstract class Player {
    protected String name;
    protected int health;
    protected int mana;
    protected int attackPower;
    protected int coins;
    protected Inventory inventory;
    protected List<Skill> skills;
    protected boolean reviveUsed;

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

    public TurnResult attack(Player target) {
        int damage = attackPower;
        target.takeDamage(damage);
        return new TurnResult(true, name + " attacks for " + damage + " damage.", damage, 0);
    }

    public void takeDamage(int amount) {
        health = Math.max(0, health - amount);
    }

    public boolean isAlive() {
        return health > 0;
    }

    public int getCoins() { return coins; }
    public void addCoins(int amount) { this.coins += amount; }

    public void spendCoins(int amount) throws NotEnoughCoinsException {
        if (coins < amount) throw new NotEnoughCoinsException(name + " has insufficient coins!");
        coins -= amount;
    }

    public Inventory getInventory() { return inventory; }
    public List<Skill> getSkills() { return skills; }
    public String getName() { return name; }
    public int getHealth() { return health; }
    public int getMana() { return mana; }
    public int getAttackPower() { return attackPower; }
    public void setHealth(int health) { this.health = health; }
    public void setMana(int mana) { this.mana = mana; }
    public boolean hasReviveUsed() { return reviveUsed; }
    public void setReviveUsed(boolean used) { this.reviveUsed = used; }

}