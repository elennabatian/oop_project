package am.aua.dungeonparty.io;

import am.aua.dungeonparty.combat.GameController;
import am.aua.dungeonparty.exceptions.SaveFileException;

import java.io.FileWriter;
import java.io.IOException;

public class SaveLoadManager {

    public static void saveGame(GameController gc, String filePath) throws SaveFileException {
        try {
            FileWriter writer = new FileWriter(filePath);

            writer.write("PLAYER,1,"
                    + gc.getPlayer1().getClass().getSimpleName() + ","
                    + gc.getPlayer1().getName() + ","
                    + gc.getPlayer1().getHealth() + ","
                    + gc.getPlayer1().getMana() + ","
                    + gc.getPlayer1().getAttackPower() + ","
                    + gc.getPlayer1().getCoins() + "\n");

            writer.write("PLAYER,2,"
                    + gc.getPlayer2().getClass().getSimpleName() + ","
                    + gc.getPlayer2().getName() + ","
                    + gc.getPlayer2().getHealth() + ","
                    + gc.getPlayer2().getMana() + ","
                    + gc.getPlayer2().getAttackPower() + ","
                    + gc.getPlayer2().getCoins() + "\n");

            writer.write("PHASE:" + gc.getPhase() + "\n");

            writer.close();

        } catch (IOException e) {
            throw new SaveFileException("Could not save the game.", e);
        }
    }
}