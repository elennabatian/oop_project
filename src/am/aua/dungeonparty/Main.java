package am.aua.dungeonparty;

import am.aua.dungeonparty.ui.GameWindow;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("cli")) {
            startCLI();
        } else {
            SwingUtilities.invokeLater(() -> new GameWindow());
        }
    }

    private static void startCLI() {
        am.aua.dungeonparty.combat.GameController controller =
                new am.aua.dungeonparty.combat.GameController();
        controller.setStore(new am.aua.dungeonparty.combat.Store());

        System.out.println("=== DUNGEON PARTY CLI ===");
        java.util.Scanner scanner = new java.util.Scanner(System.in);

        // ===== Character selection=====
        String[] classes = {"Warrior", "Mage", "Healer", "Ranger", "Rogue"};

        System.out.println("Player 1, choose a class:");
        for (int i = 0; i < classes.length; i++) {
            System.out.println((i + 1) + ") " + classes[i]);
        }
        int pick = scanner.nextInt() - 1;
        controller.chooseCharacter(1, classes[pick]);

        System.out.println("Player 2, choose a class (different from " + classes[pick] + "):");
        java.util.List<String> remaining = new java.util.ArrayList<>();
        for (String c : classes) {
            if (!c.equalsIgnoreCase(classes[pick])) {
                remaining.add(c);
                System.out.println((remaining.size()) + ") " + c);
            }
        }
        int pick2 = scanner.nextInt() - 1;
        controller.chooseCharacter(2, remaining.get(pick2));

        // ===== Shopping phase (both players) =====
        for (int playerNum = 1; playerNum <= 2; playerNum++) {
            am.aua.dungeonparty.core.Player shopper =
                    (playerNum == 1) ? controller.getPlayer1() : controller.getPlayer2();
            System.out.println("\n=== " + shopper.getName() + "'s Shop ===");
            System.out.println("Coins: " + shopper.getCoins());

            while (true) {
                System.out.println("Available items:");
                int idx = 1;
                java.util.Map<String, String> items = new java.util.LinkedHashMap<>();
                for (var entry : controller.getStore().getAvailableItems().entrySet()) {
                    System.out.println(idx + ") " + entry.getKey().getName()
                            + " - " + entry.getValue() + " coins");
                    items.put(String.valueOf(idx), entry.getKey().getName());
                    idx++;
                }
                System.out.println((idx) + ") Finish shopping");
                int choice = scanner.nextInt();
                if (choice == idx) {
                    controller.finishShopping(shopper);
                    break;
                }
                String itemName = items.get(String.valueOf(choice));
                if (itemName == null) {
                    System.out.println("Invalid choice.");
                    continue;
                }
                String result = controller.buyItem(shopper, itemName);
                System.out.println(result);
                System.out.println("Coins left: " + shopper.getCoins());
            }
        }

        // ===== Battle =====
        System.out.println("\n=== BATTLE STARTS ===");
        while (!controller.isBattleOver()) {
            am.aua.dungeonparty.core.Player attacker = controller.getCurrentAttacker();
            am.aua.dungeonparty.core.Player defender = controller.getCurrentDefender();

            System.out.println("\n" + attacker.getName() + " (HP:" + attacker.getHealth()
                    + " Mana:" + attacker.getMana() + ") vs " + defender.getName()
                    + " (HP:" + defender.getHealth() + " Mana:" + defender.getMana() + ")");
            System.out.println("Choose action: 1) Attack  2) Skill  3) Use Item  4) Combo");
            int action = scanner.nextInt();
            am.aua.dungeonparty.core.TurnResult res = null;

            switch (action) {
                case 1:
                    res = controller.processBattleAction("attack", -1);
                    break;
                case 2:
                    System.out.println("Skills:");
                    for (int i = 0; i < attacker.getSkills().size(); i++) {
                        System.out.println((i+1) + ") " + attacker.getSkills().get(i).getName());
                    }
                    int skillIdx = scanner.nextInt() - 1;
                    res = controller.processBattleAction("skill", skillIdx);
                    break;
                case 3:
                    System.out.println("Inventory:");
                    var invItems = attacker.getInventory().getItems();
                    for (int i = 0; i < invItems.size(); i++) {
                        System.out.println((i+1) + ") " + invItems.get(i).getName());
                    }
                    int itemIdx = scanner.nextInt() - 1;
                    res = controller.processBattleAction("useItem", itemIdx);
                    break;
                case 4:
                    res = controller.processBattleAction("combo", -1);
                    break;
                default:
                    System.out.println("Invalid action.");
                    continue;
            }
            if (res != null) {
                System.out.println(res.getMessage());
            }
        }
        System.out.println("Winner: " + controller.getWinner().getName());
        scanner.close();
    }
}