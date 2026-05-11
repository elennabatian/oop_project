package am.aua.dungeonparty.combat;

import am.aua.dungeonparty.exceptions.AbsenceOfSpellException;   // was AbsenceOfItemException
import am.aua.dungeonparty.inventory.Potion;
import am.aua.dungeonparty.inventory.Spell;
import am.aua.dungeonparty.inventory.Item;
import am.aua.dungeonparty.core.*;

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

        if (attacker.getMana() < skill.getManaCost()) {
            return new TurnResult(false, "Not enough mana to use " + skill.getName() + ".", 0, 0);
        }

        attacker.setMana(attacker.getMana() - skill.getManaCost());
        defender.takeDamage(skill.getDamage());

        boolean extraTurn = false;
        boolean opponentStunned = false;

        if (skill.getEffectType() == Skill.EffectType.STUN) {
            opponentStunned = true;
            extraTurn = true;
            if (attacker instanceof Ranger) {
                ((Ranger) attacker).setStunAvailable(false);
            }
        }

        String msg = attacker.getName() + " uses " + skill.getName() +
                " for " + skill.getDamage() + " damage.";
        return new TurnResult(true, msg, skill.getDamage(), 0, extraTurn, opponentStunned);
    }

    public TurnResult performUseItem(Player user, Player target, int itemIndex) {
        return user.getInventory().useItem(itemIndex, user, target);
    }

    public TurnResult performCombo(Player attacker, Player defender) {
        try {
            if (!attacker.getInventory().hasMatchingPotions(2)) {
                return new TurnResult(false, "You need two potions of the same type for a combo.", 0, 0);
            }
            Spell spell = attacker.getInventory().getFirstSpell();

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

            attacker.getInventory().removeItem(first);
            for (Item item : attacker.getInventory().getItems()) {
                if (item instanceof Potion && ((Potion)item).getType().equals(potionType)) {
                    second = (Potion) item;
                    break;
                }
            }
            if (second != null) attacker.getInventory().removeItem(second);
            attacker.getInventory().removeItem(spell);

            int damage = spell.getDamage() * 2;
            defender.takeDamage(damage);
            return new TurnResult(true,
                    attacker.getName() + " unleashes a double combo for " + damage + " damage!",
                    damage, 0);
        } catch (AbsenceOfSpellException e) {
            return new TurnResult(false, "No spell available for combo.", 0, 0);
        }
    }

    public boolean checkDeathAndRevive(Player character) {
        if (!character.isAlive() && character instanceof Healer && !character.hasReviveUsed()) {
            character.setHealth(50);
            character.setReviveUsed(true);
            return false;
        }
        return !character.isAlive();
    }
}