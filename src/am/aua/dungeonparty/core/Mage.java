package am.aua.dungeonparty.core;

import am.aua.dungeonparty.utils.GameConstants;

// Mage – starts with the Fireball skill. Normal attack deals base damage only.

public class Mage extends Player {
    public Mage() {
        super("Mage", GameConstants.MAX_HEALTH, GameConstants.MAX_MANA, 20);
        skills.add(new Skill("Fireball", GameConstants.FIREBALL_MANA,
                GameConstants.FIREBALL_DAMAGE, Skill.EffectType.DAMAGE));
    }

    @Override
    public TurnResult attack(Player target) {
        int damage = attackPower;
        target.takeDamage(damage);
        return new TurnResult(true, name + " casts a weak bolt for " + damage + " damage.", damage, 0);
    }
}