package am.aua.dungeonparty.inventory;

import am.aua.dungeonparty.core.TurnResult;

public class Spell implements Item {
    private String name;
    private int manaCost;
    private int damage;

    public Spell(String name, int manaCost, int damage) {
        this.name = name;
        this.manaCost = manaCost;
        this.damage = damage;
    }

    @Override
    public TurnResult use(am.aua.dungeonparty.core.Player user,
                          am.aua.dungeonparty.core.Player target) {
        if (user.getMana() < manaCost) {
            return new TurnResult(false, user.getName() + " does not have enough mana.", 0, 0);
        }

        user.setMana(user.getMana() - manaCost);
        target.takeDamage(damage);

        return new TurnResult(
                true,
                user.getName() + " used " + name + " and dealt " + damage + " damage.",
                damage,
                0
        );
    }

    @Override
    public String getName() {
        return name;
    }

    public int getManaCost() {
        return manaCost;
    }

    public int getDamage() {
        return damage;
    }
    @Override
    public String getDescription() {
        return "Spell: " + damage + " damage, costs " + manaCost + " mana";
    }
}