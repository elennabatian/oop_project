package am.aua.dungeonparty.core;

import am.aua.dungeonparty.utils.GameConstants;

// Ranger – possesses a one‑time StunShot skill with medium damage, stuns the opponent (skip next turn),
// and grants the Ranger an immediate extra turn.
// StunShot can only be used once per game.

public class Ranger extends Player {
    private boolean stunAvailable;

    public Ranger() {
        super("Ranger", GameConstants.MAX_HEALTH, GameConstants.MAX_MANA, 18);
        skills.add(new Skill("StunShot", GameConstants.STUN_SHOT_MANA,
                GameConstants.STUN_SHOT_DAMAGE, Skill.EffectType.STUN));
        stunAvailable = true;
    }

    // Returns true if not yet used.
    public boolean isStunAvailable() { return stunAvailable; }

    // Call after successfully using StunShot to prevent further use.
    public void setStunAvailable(boolean available) { this.stunAvailable = available; }

    @Override
    public TurnResult attack(Player target) {
        int damage = attackPower;
        target.takeDamage(damage);
        return new TurnResult(true, name + " shoots an arrow for " + damage + " damage.", damage, 0);
    }
}