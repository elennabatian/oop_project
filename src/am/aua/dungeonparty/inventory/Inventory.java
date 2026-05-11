package am.aua.dungeonparty.inventory;

import am.aua.dungeonparty.core.TurnResult;
import am.aua.dungeonparty.exceptions.AbsenceOfSpellException;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private List<Item> items;

    public Inventory() {
        items = new ArrayList<>();
    }

    public void addItem(Item item) {
        if (item != null) {
            items.add(item);
        }
    }

    public void removeItem(Item item) {
        items.remove(item);
    }
    public List<Item> getItems() {
        return items;
    }

    public TurnResult useItem(int index,
                              am.aua.dungeonparty.core.Player  user,
                              am.aua.dungeonparty.core.Player target) {

        if (index < 0 || index >= items.size()) {
            return new TurnResult(false, "Invalid item choice.", 0, 0);
        }

        Item item = items.get(index);
        TurnResult result = item.use(user, target);

        if (result.isSuccess()) {
            items.remove(index);
        }

        return result;
    }

    public boolean hasMatchingPotions(int count) {
        for (Item item : items) {
            if (item instanceof Potion) {
                Potion firstPotion = (Potion) item;
                int matches = 0;
                for (Item other : items) {
                    if (other instanceof Potion) {
                        Potion secondPotion = (Potion) other;

                        if (firstPotion.getType().equals(secondPotion.getType())) {
                            matches++;
                        }
                    }
                }

                if (matches >= count) {
                    return true;
                }
            }
        }

        return false;
    }

    public Spell getFirstSpell() throws AbsenceOfSpellException {
        for (Item item : items) {
            if (item instanceof Spell) {
                return (Spell) item;
            }
        }

        throw new AbsenceOfSpellException("No spell exists in inventory.");
    }
    public Potion getFirstMatchingPotionPairType() {
        for (Item item : items) {
            if (item instanceof Potion) {
                Potion firstPotion = (Potion) item;
                int matches = 0;

                for (Item other : items) {
                    if (other instanceof Potion) {
                        Potion secondPotion = (Potion) other;

                        if (firstPotion.getType().equals(secondPotion.getType())) {
                            matches++;
                        }
                    }
                }

                if (matches >= 2) {
                    return firstPotion;
                }
            }
        }

        return null;
    }
    public void removeTwoMatchingPotions() {
        Potion matchingPotion = getFirstMatchingPotionPairType();

        if (matchingPotion == null) {
            return;
        }

        int removed = 0;

        for (int i = items.size() - 1; i >= 0 && removed < 2; i--) {
            Item item = items.get(i);

            if (item instanceof Potion) {
                Potion potion = (Potion) item;

                if (potion.getType().equals(matchingPotion.getType())) {
                    items.remove(i);
                    removed++;
                }
            }
        }
    }
}