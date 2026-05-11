package am.aua.dungeonparty.combat;

import am.aua.dungeonparty.exceptions.AbsenceOfItemException;
import am.aua.dungeonparty.inventory.Potion;
import am.aua.dungeonparty.inventory.Spell;
import am.aua.dungeonparty.inventory.Item;

// Handles all combat actions: attacks, skills, item usage, combo moves,
// and the special Healer revive mechanic.

public class BattleManager {

    public TurnResult performAttack(Player attacker, Player defender) {
        return attacker.attack(defender);
    }

    public TurnResult performSkill(Player attacker, Player defender, int skillIndex) {
        if (skillIndex < 0 || skillIndex >= attacker.getSkills().size()) {
            return new TurnResult(false, "Invalid skill selection.", 0, 0);
        }
        Skill skill = attacker.getSkills().get(skillIndex);

        if (skill.getEffectType() == Skill.EffectType.STUN && attacker instanceof Ranger) {
            Ranger ranger = (Ranger) attacker;
            if (!ranger.isStunAvailable()) {
                return new TurnResult(false, "StunShot has already been used!", 0, 0);
            }
        }

        // Check mana
        if (attacker.getMana() < skill.getManaCost()) {
            return new TurnResult(false, "Not enough mana to use " + skill.getName() + ".", 0, 0);
        }

        // Deduct mana and apply damage
        attacker.setMana(attacker.getMana() - skill.getManaCost());
        defender.takeDamage(skill.getDamage());

        boolean extraTurn = false;
        boolean opponentStunned = false;

        if (skill.getEffectType() == Skill.EffectType.STUN) {
            opponentStunned = true;
            extraTurn = true;   // Ranger attacks again immediately after the stun
            if (attacker instanceof Ranger) {
                ((Ranger) attacker).setStunAvailable(false);  // mark as used
            }
        }

        String msg = attacker.getName() + " uses " + skill.getName() +
                " for " + skill.getDamage() + " damage.";
        return new TurnResult(true, msg, skill.getDamage(), 0, extraTurn, opponentStunned);
    }

    /**
     * Use an item from the attacker's inventory.
     */
    public TurnResult performUseItem(Player user, Player target, int itemIndex) {
        return user.getInventory().useItem(itemIndex, user, target);
    }

    /**
     * Perform the double‑effect combo: consumes two matching potions and one spell
     * to deal double the spell's damage.
     */
    public TurnResult performCombo(Player attacker, Player defender) {
        try {
            // Must have at least two potions of the same type and one spell
            if (!attacker.getInventory().hasMatchingPotions(2)) {
                return new TurnResult(false, "You need two potions of the same type for a combo.", 0, 0);
            }
            Spell spell = attacker.getInventory().getFirstSpell(); // throws if absent

            // Find and remove two matching potions
            String potionType = null;
            Potion first = null, second = null;
            for (Item item : attacker.getInventory().getItems()) {
                if (item instanceof Potion) {
                    String type = ((Potion) item).getType();
                    long count = attacker.getInventory().getItems().stream()
                            .filter(i -> i instanceof Potion && ((Potion)i).getType().equals(type))
                            .count();
                    if (count >= 2) {
                        potionType = type;
                        first = (Potion) item;
                        break;
                    }
                }
            }

            // Remove the first and find/remove the second
            attacker.getInventory().removeItem(first);
            for (Item item : attacker.getInventory().getItems()) {
                if (item instanceof Potion && ((Potion)item).getType().equals(potionType)) {
                    second = (Potion) item;
                    break;
                }
            }
            if (second != null) attacker.getInventory().removeItem(second);
            attacker.getInventory().removeItem(spell);   // consume the spel

            int damage = spell.getDamage() * 2;
            defender.takeDamage(damage);
            return new TurnResult(true,
                    attacker.getName() + " unleashes a double combo for " + damage + " damage!",
                    damage, 0);
        } catch (AbsenceOfSpellException e) {
            return new TurnResult(false, "No spell available for combo.", 0, 0);
        }
    }

    // Check if a character has died. If the character is a Healer and has not yet
    // revived, resurrect them to 50 HP and return false (still alive).
    // Returns true if the character is truly dead, false otherwise.

    public boolean checkDeathAndRevive(Player character) {
        if (!character.isAlive() && character instanceof Healer && !character.hasReviveUsed()) {
            character.setHealth(50);  // REVIVE_HEALTH
            character.setReviveUsed(true);
            return false;   // revived – not dead
        }
        return !character.isAlive();
    }
}