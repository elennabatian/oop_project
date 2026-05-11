package am.aua.dungeonparty.core;

import am.aua.dungeonparty.utils.GameConstants;

// Rogue – reduces all incoming damage by 40%.

public class Rogue extends Player {
    public Rogue() {
        super("Rogue", GameConstants.MAX_HEALTH, GameConstants.MAX_MANA, 20);
    }

    @Override
    public void takeDamage(int amount) {
        int reduced = (int)(amount * (1 - GameConstants.ROGUE_DAMAGE_REDUCTION));
        super.takeDamage(reduced);
    }

    @Override
    public TurnResult attack(Player target) {
        int damage = attackPower;
        target.takeDamage(damage);
        return new TurnResult(true, name + " backstabs for " + damage + " damage.", damage, 0);
    }
}