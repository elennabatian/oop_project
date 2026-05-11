package am.aua.dungeonparty.combat;

import am.aua.dungeonparty.exceptions.NotEnoughCoinsException;
import am.aua.dungeonparty.inventory.Item;
import am.aua.dungeonparty.inventory.Potion;
import am.aua.dungeonparty.inventory.Spell;
import am.aua.dungeonparty.utils.GameConstants;

import java.util.LinkedHashMap;
import java.util.Map;

public class Store {
    private LinkedHashMap<Item, Integer> stock;

    public Store() {
        stock = new LinkedHashMap<>();

        stock.put(new Potion("Health Potion", "Healing", GameConstants.HEALTH_POTION_HEAL),
                GameConstants.HEALTH_POTION_PRICE);

        stock.put(new Potion("Strength Potion", "Strength", 0),
                GameConstants.STRENGTH_POTION_PRICE);

        stock.put(new Spell("Fire Blast", 20, 35),
                GameConstants.SPELL_PRICE);

        stock.put(new Spell("Ice Shard", 15, 25),
                GameConstants.SPELL_PRICE);
    }

    public Map<Item, Integer> getAvailableItems() {
        return stock;
    }

    public Item buyItem(String itemName,
                        am.aua.dungeonparty.core.Player buyer)
            throws NotEnoughCoinsException {

        for (Item item : stock.keySet()) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                int price = stock.get(item);

                if (buyer.getCoins() < price) {
                    throw new
                            NotEnoughCoinsException("Not enough coins to buy " + itemName + ".");
                }

                buyer.spendCoins(price);
                return createNewItemCopy(item);
            }
        }

        return null;
    }

    private Item createNewItemCopy(Item item) {
        if (item instanceof Potion) {
            Potion potion = (Potion) item;
            return new Potion(potion.getName(), potion.getType(), potion.getHealingAmount());
        }

        if (item instanceof Spell) {
            Spell spell = (Spell) item;
            return new Spell(spell.getName(), spell.getManaCost(), spell.getDamage());
        }

        return null;
    }
}