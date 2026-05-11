package am.aua.dungeonparty.core;

import am.aua.dungeonparty.utils.GameConstants;

// Healer – can resurrect once per game when health drops to 0.

public class Healer extends Player {
    public Healer() {
        super("Healer", GameConstants.MAX_HEALTH, GameConstants.MAX_MANA, 15);
    }

    @Override
    public TurnResult attack(Player target) {
        int damage = attackPower;
        target.takeDamage(damage);
        return new TurnResult(true, name + " strikes for " + damage + " damage.", damage, 0);
    }
}