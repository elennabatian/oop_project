package am.aua.dungeonparty.core;

import am.aua.dungeonparty.utils.GameConstants;

// Warrior – receives a passive 40% attack power boost on every attack.

public class Warrior extends Player {
    public Warrior() {
        super("Warrior", GameConstants.MAX_HEALTH, GameConstants.MAX_MANA,
                (int)(25 * (1 + GameConstants.WARRIOR_ATTACK_BONUS))); // base attackPower = 25 * 1.4
    }

    // Overridden to apply the 40% bonus exactly as required.
    @Override
    public TurnResult attack(Player target) {
        int damage = (int)(attackPower);  // already boosted in constructor
        target.takeDamage(damage);
        return new TurnResult(true, name + " slashes for " + damage + " damage.", damage, 0);
    }
}