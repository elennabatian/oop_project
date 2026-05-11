package am.aua.dungeonparty.inventory;

import am.aua.dungeonparty.core.TurnResult;

public abstract  class Potion implements Item {
    private String name;
    private String type;
    private int healingAmount;

    public Potion(String name, String type, int healingAmount) {
        this.name = name;
        this.type = type;
        this.healingAmount = healingAmount;
    }

    @Override
    public TurnResult use(am.aua.dungeonparty.core.Player user,
                          am.aua.dungeonparty.core.Player target) {
        if (type.equals("Healing")) {
            user.setHealth(user.getHealth() + healingAmount);

            return new TurnResult(
                    true,
                    user.getName() + " used " + name + " and healed " + healingAmount + " HP.",
                    0,
                    0
            );
        }

        if (type.equals("Strength")) {
            return new TurnResult(
                    false,
                    "Strength Potion cannot be used alone. It is only used for combos.",
                    0,
                    0
            );
        }

        return new TurnResult(false, "Unknown potion type.", 0, 0);
    }

    @Override
    public String getName() {
        return name;
    }
    public String getType() {
        return type;
    }

    public int getHealingAmount() {
        return healingAmount;
    }

    @Override
    public String getDescription() {
        return type + " potion";
    }
}