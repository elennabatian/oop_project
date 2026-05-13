package am.aua.dungeonparty.io;

import am.aua.dungeonparty.combat.GameController;
import am.aua.dungeonparty.combat.GameController.Phase;
import am.aua.dungeonparty.core.*;
import am.aua.dungeonparty.exceptions.SaveFileException;
import am.aua.dungeonparty.utils.GameConstants;

import java.io.*;

public class SaveLoadManager {

    public static void saveGame(GameController gc, String filePath) throws SaveFileException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            writePlayer(pw, gc.getPlayer1(), 1);
            writePlayer(pw, gc.getPlayer2(), 2);
            pw.println("PHASE:" + gc.getPhase());
        } catch (IOException e) {
            throw new SaveFileException("Could not save the game.", e);
        }
    }

    private static void writePlayer(PrintWriter pw, Player p, int num) {
        if (p == null) return;
        pw.printf("PLAYER,%d,%s,%s,%d,%d,%d,%d\n",
                num,
                p.getClass().getSimpleName(),
                p.getName(),
                p.getHealth(),
                p.getMana(),
                p.getAttackPower(),
                p.getCoins());
    }

    public static GameController loadGame(String filePath) throws SaveFileException {
        GameController gc = new GameController();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            Player p1 = null, p2 = null;
            Phase phase = Phase.START;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("PLAYER,")) {
                    String[] parts = line.split(",");
                    int playerNum = Integer.parseInt(parts[1]);
                    String className = parts[2];
                    // parts[3] name (ignored, constructor sets name)
                    int health = Integer.parseInt(parts[4]);
                    int mana = Integer.parseInt(parts[5]);
                    int coins = Integer.parseInt(parts[7]);

                    Player player = createPlayer(className);
                    if (player != null) {
                        player.setHealth(health);
                        player.setMana(mana);
                        int coinDiff = coins - player.getCoins();
                        if (coinDiff > 0) {
                            player.addCoins(coinDiff);
                        } else if (coinDiff < 0) {
                            setPlayerCoins(player, coins);
                        }
                        if (playerNum == 1) {
                            p1 = player;
                        } else {
                            p2 = player;
                        }
                    }
                } else if (line.startsWith("PHASE:")) {
                    String phaseStr = line.substring(6).trim();
                    try {
                        phase = Phase.valueOf(phaseStr);
                    } catch (IllegalArgumentException ignored) {

                    }
                }
            }

            if (p1 != null && p2 != null) {
                gc.initializeLoadedGame(p1, p2, phase);
            } else {
                throw new SaveFileException("Missing player data in save file.", null);
            }
        } catch (IOException e) {
            throw new SaveFileException("Could not load the game.", e);
        }
        return gc;
    }

    private static Player createPlayer(String className) {
        switch (className.toLowerCase()) {
            case "warrior": return new Warrior();
            case "mage":    return new Mage();
            case "healer":  return new Healer();
            case "ranger":  return new Ranger();
            case "rogue":   return new Rogue();
            default:        return null;
        }
    }


    private static void setPlayerCoins(Player p, int amount) {
        p.setCoins(amount);
    }
}