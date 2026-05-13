package am.aua.dungeonparty.combat;

import am.aua.dungeonparty.core.*;
import am.aua.dungeonparty.inventory.Item;
import am.aua.dungeonparty.exceptions.NotEnoughCoinsException;   // added

public class GameController {
    private Player player1;
    private Player player2;
    private BattleManager battleManager;
    private Store store;
    private boolean battleOver;
    private Player currentAttacker;
    private Player currentDefender;
    private boolean opponentStunned;

    public enum Phase { START, PICK1, PICK2, SHOP1, SHOP2, BATTLE, END }
    private Phase phase;

    public GameController() {
        battleManager = new BattleManager();
        store = null;
        phase = Phase.START;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Phase getPhase() { return phase; }

    private Player createCharacter(String className) {
        switch (className.toLowerCase()) {
            case "warrior": return new Warrior();
            case "mage":    return new Mage();
            case "healer":  return new Healer();
            case "ranger":  return new Ranger();
            case "rogue":   return new Rogue();
            default:        return null;
        }
    }

    public boolean chooseCharacter(int playerNum, String className) {
        Player chosen = createCharacter(className);
        if (chosen == null) return false;

        if (playerNum == 1) {
            if (player1 != null) return false;
            player1 = chosen;
            phase = Phase.PICK2;
        } else {
            if (player2 != null || player1 == null) return false;
            if (player1.getClass().equals(chosen.getClass())) return false;
            player2 = chosen;
            phase = Phase.SHOP1;
        }
        return true;
    }

    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }

    public String buyItem(Player buyer, String itemName) {
        if (store == null) return "Store not available.";
        try {
            Item item = store.buyItem(itemName, buyer);
            if (item != null) {
                buyer.getInventory().addItem(item);
                return buyer.getName() + " bought " + item.getName() + ".";
            }
        } catch (NotEnoughCoinsException e) {   // now using the external exception
            return e.getMessage();
        }
        return "Item not available.";
    }

    public void finishShopping(Player player) {
        if (phase == Phase.SHOP1 && player == player1) {
            phase = Phase.SHOP2;
        } else if (phase == Phase.SHOP2 && player == player2) {
            phase = Phase.BATTLE;
            currentAttacker = player1;
            currentDefender = player2;
            opponentStunned = false;
        }
    }

    public Store getStore() { return store; }

    public TurnResult processBattleAction(String actionType, int param) {
        if (phase != Phase.BATTLE || battleOver)
            return new TurnResult(false, "Battle is not active.", 0, 0);

        TurnResult res;
        switch (actionType) {
            case "attack":
                res = battleManager.performAttack(currentAttacker, currentDefender);
                break;
            case "skill":
                res = battleManager.performSkill(currentAttacker, currentDefender, param);
                break;
            case "useItem":
                res = battleManager.performUseItem(currentAttacker, currentDefender, param);
                break;
            case "combo":
                res = battleManager.performCombo(currentAttacker, currentDefender);
                break;
            default:
                return new TurnResult(false, "Unknown action.", 0, 0);
        }

        boolean trulyDead = battleManager.checkDeathAndRevive(currentDefender);
        if (trulyDead) {
            battleOver = true;
            phase = Phase.END;
            String winMsg = currentDefender.getName() + " is defeated! " + currentAttacker.getName() + " wins!";
            return new TurnResult(true, winMsg, res.getDamageDone(), res.getCoinsChanged());
        }

        if (res.grantsExtraTurn()) {
            opponentStunned = res.isOpponentStunned();
        } else if (opponentStunned) {
            opponentStunned = false;
        } else {
            Player temp = currentAttacker;
            currentAttacker = currentDefender;
            currentDefender = temp;
        }
        return res;
    }

    public Player getCurrentAttacker() { return currentAttacker; }
    public Player getCurrentDefender() { return currentDefender; }
    public boolean isBattleOver() { return battleOver; }

    public Player getWinner() {
        if (!battleOver) return null;
        return player1.isAlive() ? player1 : player2;
    }

    public void initializeLoadedGame(Player p1, Player p2, Phase phase) {
        this.player1 = p1;
        this.player2 = p2;
        this.phase = phase;
        this.battleOver = false;
        this.opponentStunned = false;
        if (phase == Phase.BATTLE) {
            currentAttacker = p1;
            currentDefender = p2;
        }
    }

}