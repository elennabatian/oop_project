package am.aua.dungeonparty.core;

// Represents a special ability with a mana cost, damage, and an effect type.

public class Skill {
    public enum EffectType { DAMAGE, STUN }

    private String name;
    private int manaCost;
    private int damage;
    private EffectType effectType;

    public Skill(String name, int manaCost, int damage, EffectType effectType) {
        this.name = name;
        this.manaCost = manaCost;
        this.damage = damage;
        this.effectType = effectType;
    }

    public String getName() { return name; }
    public int getManaCost() { return manaCost; }
    public int getDamage() { return damage; }
    public EffectType getEffectType() { return effectType; }
}