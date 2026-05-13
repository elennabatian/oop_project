package am.aua.dungeonparty.ui;

import am.aua.dungeonparty.combat.GameController;
import am.aua.dungeonparty.combat.Store;
import am.aua.dungeonparty.core.Player;
import am.aua.dungeonparty.core.Skill;
import am.aua.dungeonparty.inventory.Item;
import am.aua.dungeonparty.utils.GameConstants;
import am.aua.dungeonparty.io.SaveLoadManager;
import am.aua.dungeonparty.exceptions.SaveFileException;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;

/**
 * GameWindow – the entire Swing front-end for Dungeon Party.
 *
 * Layout uses a CardLayout so each "screen" is its own JPanel:
 *   WELCOME  → character picking → shop → battle → end screen
 *
 * All colours, fonts, and drawing are done in plain Java2D — no external images needed.
 * Character art is rendered as simple geometric shapes so the game works out of the box.
 */
public class GameWindow extends JFrame {

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(18, 12, 30);
    private static final Color BG_MID       = new Color(35, 22, 55);
    private static final Color ACCENT_GOLD  = new Color(255, 200, 50);
    private static final Color ACCENT_RED   = new Color(200, 50, 50);
    private static final Color ACCENT_BLUE  = new Color(60, 130, 220);
    private static final Color ACCENT_GREEN = new Color(50, 180, 80);
    private static final Color TEXT_LIGHT   = new Color(230, 220, 255);
    private static final Color HP_COLOR     = new Color(220, 60, 60);
    private static final Color MANA_COLOR   = new Color(60, 100, 220);
    private static final Color PANEL_BG     = new Color(28, 18, 45);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private static final Font TITLE_FONT   = new Font("Serif",     Font.BOLD,  36);
    private static final Font HEADER_FONT  = new Font("SansSerif", Font.BOLD,  18);
    private static final Font BODY_FONT    = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font SMALL_FONT   = new Font("SansSerif", Font.PLAIN, 11);
    private static final Font BTN_FONT     = new Font("SansSerif", Font.BOLD,  14);

    // ── Card names ────────────────────────────────────────────────────────────
    private static final String CARD_WELCOME  = "WELCOME";
    private static final String CARD_PICK     = "PICK";
    private static final String CARD_SHOP     = "SHOP";
    private static final String CARD_BATTLE   = "BATTLE";
    private static final String CARD_END      = "END";

    // ── State ─────────────────────────────────────────────────────────────────
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel     rootPanel  = new JPanel(cardLayout);
    private GameController   gc;
    private int              currentPickingPlayer; // 1 or 2

    // Battle screen references we update each turn
    private JLabel   p1NameLabel, p2NameLabel;
    private JLabel   p1HpLabel,   p2HpLabel;
    private JLabel   p1MpLabel,   p2MpLabel;
    private JProgressBar p1HpBar, p2HpBar;
    private JProgressBar p1MpBar, p2MpBar;
    private JTextArea    battleLog;
    private JPanel       actionPanel;
    private JLabel       turnLabel;

    // ─────────────────────────────────────────────────────────────────────────
    public GameWindow() {
        super("⚔  Dungeon Party");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setResizable(false);

        // Dark title bar if supported
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}

        rootPanel.setBackground(BG_DARK);

        // Build every screen and add it to the card deck
        rootPanel.add(buildWelcomeScreen(), CARD_WELCOME);
        rootPanel.add(buildPickScreen(),    CARD_PICK);
        rootPanel.add(buildShopScreen(),    CARD_SHOP);
        rootPanel.add(buildBattleScreen(),  CARD_BATTLE);
        rootPanel.add(buildEndScreen(),     CARD_END);

        add(rootPanel);
        cardLayout.show(rootPanel, CARD_WELCOME);
        setVisible(true);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  WELCOME SCREEN
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildWelcomeScreen() {
        // Custom-painted gradient background panel
        JPanel panel = new GradientPanel(BG_DARK, BG_MID) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw decorative sword/shield shapes in the background
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(80, 50, 120, 60));
                // Left ornament
                g2.fillOval(-60, 100, 200, 200);
                // Right ornament
                g2.fillOval(getWidth() - 140, 300, 200, 200);
            }
        };
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;

        // ── Logo / title ──
        JLabel logo = new JLabel("⚔  DUNGEON PARTY  ⚔", SwingConstants.CENTER);
        logo.setFont(TITLE_FONT);
        logo.setForeground(ACCENT_GOLD);
        gbc.gridy = 0;
        panel.add(logo, gbc);

        JLabel sub = new JLabel("A Player-vs-Player Dungeon Brawler", SwingConstants.CENTER);
        sub.setFont(BODY_FONT);
        sub.setForeground(TEXT_LIGHT);
        gbc.gridy = 1;
        panel.add(sub, gbc);

        // ── Start button ──
        JButton startBtn = makeButton("▶  START GAME", ACCENT_GOLD, BG_DARK);
        startBtn.addActionListener(e -> startNewGame());
        gbc.gridy = 2;
        gbc.insets = new Insets(30, 10, 5, 10);
        panel.add(startBtn, gbc);

        // ── Load button ──
        JButton loadBtn = makeButton("📂  LOAD GAME", ACCENT_GREEN, BG_DARK);
        loadBtn.addActionListener(e -> loadGame());
        gbc.gridy = 3;
        gbc.insets = new Insets(5, 10, 5, 10);
        panel.add(loadBtn, gbc);

        // ── Rules button ──
        JButton rulesBtn = makeButton("📜  RULES", ACCENT_BLUE, BG_DARK);
        rulesBtn.addActionListener(e -> showRulesDialog());
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 10, 10, 10);
        panel.add(rulesBtn, gbc);

        return panel;
    }

    /** Resets everything and jumps to the character-pick screen for Player 1. */
    private void startNewGame() {
        gc = new GameController();
        gc.setStore(new Store());
        currentPickingPlayer = 1;
        refreshPickScreen();
        cardLayout.show(rootPanel, CARD_PICK);
    }

    /** Attempts to load a saved game from "savegame.csv". */
    private void loadGame() {
        try {
            gc = SaveLoadManager.loadGame("savegame.csv");
            gc.setStore(new Store()); // ensure store is available
            currentPickingPlayer = 1; // not used after load, but safe
            // After loading, we must determine which phase to show
            if (gc.getPhase() == GameController.Phase.BATTLE) {
                refreshBattleScreen();
                cardLayout.show(rootPanel, CARD_BATTLE);
            } else if (gc.getPhase() == GameController.Phase.SHOP1 ||
                    gc.getPhase() == GameController.Phase.SHOP2) {
                // Load mid-shop: determine current shopping player
                if (gc.getPhase() == GameController.Phase.SHOP1) {
                    refreshShopScreen(gc.getPlayer1());
                } else {
                    refreshShopScreen(gc.getPlayer2());
                }
                cardLayout.show(rootPanel, CARD_SHOP);
            } else {
                // Fallback to character pick
                refreshPickScreen();
                cardLayout.show(rootPanel, CARD_PICK);
            }
            JOptionPane.showMessageDialog(this, "Game loaded successfully.", "Load Game", JOptionPane.INFORMATION_MESSAGE);
        } catch (SaveFileException ex) {
            showMsg("Failed to load game: " + ex.getMessage(), "Load Error");
        }
    }

    /** Pops up a dialog with all the game rules extracted from the code comments. */
    private void showRulesDialog() {
        String rules =
                "═══════════════════════════════════════\n" +
                        "          DUNGEON PARTY – RULES\n" +
                        "═══════════════════════════════════════\n\n" +
                        "CHARACTERS\n" +
                        "──────────\n" +
                        "• Warrior  – 40% attack bonus baked in. High damage dealer.\n" +
                        "• Mage     – Starts with Fireball skill (20 mana, 30 dmg).\n" +
                        "• Healer   – One-time auto-revive at 50 HP when killed.\n" +
                        "• Ranger   – One-time StunShot (25 mana, 20 dmg);\n" +
                        "             stuns opponent (they skip next turn) and\n" +
                        "             grants Ranger an extra turn immediately.\n" +
                        "• Rogue    – Reduces ALL incoming damage by 40%.\n\n" +
                        "SHOP (before battle)\n" +
                        "────────────────────\n" +
                        "• Health Potion   – 100 coins  → restores 30 HP\n" +
                        "• Strength Potion – 150 coins  → needed for combo\n" +
                        "• Fire Blast      – 200 coins  → 35 dmg, costs 20 mana\n" +
                        "• Ice Shard       – 200 coins  → 25 dmg, costs 15 mana\n" +
                        "Each player starts with " + GameConstants.STARTING_COINS + " coins.\n\n" +
                        "BATTLE\n" +
                        "──────\n" +
                        "• Player 1 always goes first.\n" +
                        "• Each turn: Attack / Use Skill / Use Item / Combo.\n" +
                        "• Combo: uses 2 matching potions + 1 spell = double spell damage.\n" +
                        "• Mana is consumed when using skills/spells.\n" +
                        "• The player whose HP reaches 0 loses\n" +
                        "  (Healer gets one free revive at 50 HP).\n";

        // Dark-themed scroll pane dialog
        JTextArea ta = new JTextArea(rules);
        ta.setEditable(false);
        ta.setFont(BODY_FONT);
        ta.setBackground(PANEL_BG);
        ta.setForeground(TEXT_LIGHT);
        ta.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JScrollPane scroll = new JScrollPane(ta);
        scroll.setPreferredSize(new Dimension(480, 400));
        scroll.getViewport().setBackground(PANEL_BG);

        JOptionPane.showMessageDialog(this, scroll, "📜 Game Rules",
                JOptionPane.PLAIN_MESSAGE);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CHARACTER PICK SCREEN
    // ═════════════════════════════════════════════════════════════════════════

    // We keep references so refreshPickScreen() can update them without rebuilding.
    private JLabel pickTitleLabel;
    private JPanel pickCardsPanel;

    private JPanel buildPickScreen() {
        JPanel panel = new GradientPanel(BG_DARK, BG_MID);
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ── Title ──
        pickTitleLabel = new JLabel("Player 1 – Choose Your Hero", SwingConstants.CENTER);
        pickTitleLabel.setFont(HEADER_FONT);
        pickTitleLabel.setForeground(ACCENT_GOLD);
        panel.add(pickTitleLabel, BorderLayout.NORTH);

        // ── Character cards ──
        pickCardsPanel = new JPanel(new GridLayout(1, 5, 12, 0));
        pickCardsPanel.setOpaque(false);
        panel.add(pickCardsPanel, BorderLayout.CENTER);

        // Character data: { name, colour, description }
        Object[][] chars = {
                { "Warrior", new Color(180, 100, 30),
                        "ATK: 35\nHP:  100\nMana:100\n\nPassive:\n+40% attack\npower on\nevery hit." },
                { "Mage", new Color(80, 60, 180),
                        "ATK: 20\nHP:  100\nMana:100\n\nSkill:\nFireball\n30 dmg\n20 mana" },
                { "Healer", new Color(60, 160, 80),
                        "ATK: 15\nHP:  100\nMana:100\n\nPassive:\nAuto-revive\nonce at\n50 HP" },
                { "Ranger", new Color(120, 160, 40),
                        "ATK: 18\nHP:  100\nMana:100\n\nSkill:\nStunShot\n20 dmg\nStuns foe" },
                { "Rogue", new Color(160, 30, 80),
                        "ATK: 20\nHP:  100\nMana:100\n\nPassive:\n-40% incoming\ndamage\n(evasion)" }
        };

        for (Object[] c : chars) {
            String charName  = (String) c[0];
            Color  charColor = (Color)  c[1];
            String desc      = (String) c[2];
            pickCardsPanel.add(buildCharCard(charName, charColor, desc));
        }

        // ── Back button ──
        JButton backBtn = makeButton("← Back", TEXT_LIGHT, BG_MID);
        backBtn.addActionListener(e -> cardLayout.show(rootPanel, CARD_WELCOME));
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.setOpaque(false);
        south.add(backBtn);
        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    /** Builds a clickable card for one character. */
    private JPanel buildCharCard(String charName, Color accent, String descText) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(PANEL_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(accent, 2, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        // Art panel – drawn with Java2D shapes
        CharArtPanel art = new CharArtPanel(charName, accent);
        art.setPreferredSize(new Dimension(120, 140));
        card.add(art, BorderLayout.NORTH);

        // Name label
        JLabel nameLabel = new JLabel(charName, SwingConstants.CENTER);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        nameLabel.setForeground(accent);
        card.add(nameLabel, BorderLayout.CENTER);

        // Stats text area
        JTextArea desc = new JTextArea(descText);
        desc.setEditable(false);
        desc.setFont(SMALL_FONT);
        desc.setBackground(PANEL_BG);
        desc.setForeground(TEXT_LIGHT);
        desc.setMargin(new Insets(4, 4, 4, 4));
        card.add(desc, BorderLayout.SOUTH);

        // Hover highlight
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(50, 35, 75));
                art.setHighlighted(true);
                card.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                card.setBackground(PANEL_BG);
                art.setHighlighted(false);
                card.repaint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                handleCharacterPick(charName);
            }
        });

        return card;
    }

    /** Called when a character card is clicked. */
    private void handleCharacterPick(String charName) {
        // Check player 2 isn't picking the same class
        if (currentPickingPlayer == 2 && gc.getPlayer1() != null
                && gc.getPlayer1().getClass().getSimpleName().equalsIgnoreCase(charName)) {
            showMsg("⚠  Player 2 must pick a different class than Player 1!", "Class Conflict");
            return;
        }

        boolean ok = gc.chooseCharacter(currentPickingPlayer, charName);
        if (!ok) {
            showMsg("Invalid selection – please try again.", "Error");
            return;
        }

        if (currentPickingPlayer == 1) {
            currentPickingPlayer = 2;
            refreshPickScreen();
        } else {
            // Both picked – go to shop for Player 1
            refreshShopScreen(gc.getPlayer1());
            cardLayout.show(rootPanel, CARD_SHOP);
        }
    }

    /** Updates the pick-screen title after Player 1 has chosen. */
    private void refreshPickScreen() {
        String who = (currentPickingPlayer == 1) ? "Player 1" : "Player 2";
        pickTitleLabel.setText(who + " – Choose Your Hero");
        pickTitleLabel.setForeground(currentPickingPlayer == 1 ? ACCENT_GOLD : ACCENT_BLUE);
        pickTitleLabel.repaint();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  SHOP SCREEN
    // ═════════════════════════════════════════════════════════════════════════

    private JLabel   shopTitleLabel;
    private JLabel   shopCoinsLabel;
    private JTextArea shopLog;
    private Player   currentShopPlayer; // which player is shopping right now

    private JPanel buildShopScreen() {
        JPanel panel = new GradientPanel(BG_DARK, BG_MID);
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // ── Title row ──
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        shopTitleLabel = new JLabel("🛒  Player 1's Shop", SwingConstants.LEFT);
        shopTitleLabel.setFont(HEADER_FONT);
        shopTitleLabel.setForeground(ACCENT_GOLD);
        titleRow.add(shopTitleLabel, BorderLayout.WEST);

        shopCoinsLabel = new JLabel("Coins: 2000", SwingConstants.RIGHT);
        shopCoinsLabel.setFont(HEADER_FONT);
        shopCoinsLabel.setForeground(ACCENT_GOLD);
        titleRow.add(shopCoinsLabel, BorderLayout.EAST);

        panel.add(titleRow, BorderLayout.NORTH);

        // ── Item grid ──
        JPanel itemGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        itemGrid.setOpaque(false);

        // Item cards: { display name, internal name, icon, desc }
        Object[][] items = {
                { "Health Potion",   "Health Potion",   "🧪", "Restores 30 HP\nCost: 100 coins" },
                { "Strength Potion", "Strength Potion", "💪", "Boosts strength\nCost: 150 coins" },
                { "Fire Blast",      "Fire Blast",      "🔥", "35 dmg / 20 mana\nCost: 200 coins" },
                { "Ice Shard",       "Ice Shard",       "❄️", "25 dmg / 15 mana\nCost: 200 coins" }
        };

        for (Object[] item : items) {
            String display  = (String) item[0];
            String internal = (String) item[1];
            String icon     = (String) item[2];
            String desc     = (String) item[3];
            itemGrid.add(buildShopItemCard(display, internal, icon, desc));
        }

        panel.add(itemGrid, BorderLayout.CENTER);

        // ── Bottom: log + done button ──
        JPanel south = new JPanel(new BorderLayout(8, 0));
        south.setOpaque(false);

        shopLog = new JTextArea(3, 40);
        shopLog.setEditable(false);
        shopLog.setFont(SMALL_FONT);
        shopLog.setBackground(PANEL_BG);
        shopLog.setForeground(ACCENT_GREEN);
        shopLog.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(ACCENT_GOLD, 1), "Purchase Log",
                TitledBorder.LEFT, TitledBorder.TOP, SMALL_FONT, ACCENT_GOLD));
        south.add(new JScrollPane(shopLog), BorderLayout.CENTER);

        JButton doneBtn = makeButton("✅  Done Shopping", ACCENT_GREEN, BG_DARK);
        doneBtn.addActionListener(e -> finishShop());
        south.add(doneBtn, BorderLayout.EAST);

        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    /** One clickable item card in the shop. */
    private JPanel buildShopItemCard(String displayName, String internalName,
                                     String icon, String desc) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(PANEL_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(ACCENT_GOLD, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JLabel iconLabel = new JLabel(icon + "  " + displayName, SwingConstants.LEFT);
        iconLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        iconLabel.setForeground(ACCENT_GOLD);
        card.add(iconLabel, BorderLayout.NORTH);

        JTextArea descArea = new JTextArea(desc);
        descArea.setEditable(false);
        descArea.setFont(BODY_FONT);
        descArea.setBackground(PANEL_BG);
        descArea.setForeground(TEXT_LIGHT);
        card.add(descArea, BorderLayout.CENTER);

        JButton buyBtn = makeButton("Buy", ACCENT_GOLD, BG_DARK);
        buyBtn.setFont(BTN_FONT);
        buyBtn.addActionListener(e -> {
            String result = gc.buyItem(currentShopPlayer, internalName);
            shopLog.append(result + "\n");
            shopCoinsLabel.setText("Coins: " + currentShopPlayer.getCoins());
        });
        card.add(buyBtn, BorderLayout.SOUTH);

        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { card.setBackground(new Color(45, 30, 65)); }
            @Override public void mouseExited (MouseEvent e) { card.setBackground(PANEL_BG); }
        });

        return card;
    }

    /** Refreshes coin label and title for the given shopping player. */
    private void refreshShopScreen(Player p) {
        currentShopPlayer = p;
        String who = (p == gc.getPlayer1()) ? "Player 1" : "Player 2";
        shopTitleLabel.setText("🛒  " + who + "'s Shop  (" + p.getName() + ")");
        shopCoinsLabel.setText("Coins: " + p.getCoins());
        shopLog.setText("");
    }

    /** Called when "Done Shopping" is clicked. */
    private void finishShop() {
        gc.finishShopping(currentShopPlayer);

        if (gc.getPhase() == GameController.Phase.SHOP2) {
            // Player 1 is done → show shop for Player 2
            refreshShopScreen(gc.getPlayer2());
        } else if (gc.getPhase() == GameController.Phase.BATTLE) {
            // Both done → start battle
            refreshBattleScreen();
            cardLayout.show(rootPanel, CARD_BATTLE);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  BATTLE SCREEN
    // ═════════════════════════════════════════════════════════════════════════

    private JPanel buildBattleScreen() {
        JPanel panel = new GradientPanel(BG_DARK, BG_MID);
        panel.setLayout(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // ── Top: two stat blocks ──
        JPanel statsRow = new JPanel(new GridLayout(1, 2, 20, 0));
        statsRow.setOpaque(false);
        statsRow.add(buildPlayerStatBlock(1));
        statsRow.add(buildPlayerStatBlock(2));
        panel.add(statsRow, BorderLayout.NORTH);

        // ── Centre: battle log ──
        battleLog = new JTextArea();
        battleLog.setEditable(false);
        battleLog.setFont(BODY_FONT);
        battleLog.setBackground(PANEL_BG);
        battleLog.setForeground(TEXT_LIGHT);
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);
        battleLog.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JScrollPane logScroll = new JScrollPane(battleLog);
        logScroll.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(ACCENT_GOLD, 1), "Battle Log",
                TitledBorder.LEFT, TitledBorder.TOP, SMALL_FONT, ACCENT_GOLD));
        logScroll.setPreferredSize(new Dimension(860, 160));
        panel.add(logScroll, BorderLayout.CENTER);

        // ── Bottom: turn label + action buttons + save button ──
        JPanel southPanel = new JPanel(new BorderLayout(6, 6));
        southPanel.setOpaque(false);

        turnLabel = new JLabel("⚔  Player 1's Turn  (Warrior)", SwingConstants.CENTER);
        turnLabel.setFont(HEADER_FONT);
        turnLabel.setForeground(ACCENT_GOLD);
        southPanel.add(turnLabel, BorderLayout.NORTH);

        actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        actionPanel.setOpaque(false);
        southPanel.add(actionPanel, BorderLayout.CENTER);

        // Save button (bottom-right)
        JPanel saveRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveRow.setOpaque(false);
        JButton saveBtn = makeButton("💾 Save Game", ACCENT_GOLD, BG_DARK);
        saveBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        saveBtn.addActionListener(e -> saveCurrentGame());
        saveRow.add(saveBtn);
        southPanel.add(saveRow, BorderLayout.SOUTH);

        panel.add(southPanel, BorderLayout.SOUTH);
        return panel;
    }

    /** Builds the HP/mana block for one player (called once; labels updated later). */
    private JPanel buildPlayerStatBlock(int playerNum) {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setBackground(PANEL_BG);
        block.setBorder(new CompoundBorder(
                new LineBorder(playerNum == 1 ? ACCENT_GOLD : ACCENT_BLUE, 2, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        String prefix = "P" + playerNum;

        JLabel nameLabel = new JLabel("Player " + playerNum, SwingConstants.LEFT);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        nameLabel.setForeground(playerNum == 1 ? ACCENT_GOLD : ACCENT_BLUE);
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel hpLabel  = new JLabel("HP:   100 / 100");
        hpLabel.setFont(SMALL_FONT);
        hpLabel.setForeground(HP_COLOR);
        hpLabel.setAlignmentX(LEFT_ALIGNMENT);

        JProgressBar hpBar = new JProgressBar(0, GameConstants.MAX_HEALTH);
        hpBar.setValue(GameConstants.MAX_HEALTH);
        hpBar.setForeground(HP_COLOR);
        hpBar.setBackground(new Color(60, 20, 20));
        hpBar.setPreferredSize(new Dimension(200, 12));
        hpBar.setMaximumSize(new Dimension(400, 12));
        hpBar.setAlignmentX(LEFT_ALIGNMENT);

        JLabel mpLabel  = new JLabel("Mana: 100 / 100");
        mpLabel.setFont(SMALL_FONT);
        mpLabel.setForeground(MANA_COLOR);
        mpLabel.setAlignmentX(LEFT_ALIGNMENT);

        JProgressBar mpBar = new JProgressBar(0, GameConstants.MAX_MANA);
        mpBar.setValue(GameConstants.MAX_MANA);
        mpBar.setForeground(MANA_COLOR);
        mpBar.setBackground(new Color(20, 20, 60));
        mpBar.setPreferredSize(new Dimension(200, 12));
        mpBar.setMaximumSize(new Dimension(400, 12));
        mpBar.setAlignmentX(LEFT_ALIGNMENT);

        block.add(nameLabel);
        block.add(Box.createVerticalStrut(4));
        block.add(hpLabel);
        block.add(hpBar);
        block.add(Box.createVerticalStrut(3));
        block.add(mpLabel);
        block.add(mpBar);

        // Store references for later updates
        if (playerNum == 1) {
            p1NameLabel = nameLabel; p1HpLabel = hpLabel; p1HpBar = hpBar;
            p1MpLabel   = mpLabel;   p1MpBar   = mpBar;
        } else {
            p2NameLabel = nameLabel; p2HpLabel = hpLabel; p2HpBar = hpBar;
            p2MpLabel   = mpLabel;   p2MpBar   = mpBar;
        }

        return block;
    }

    /** Called once when battle begins: sets names, stats, and first action panel. */
    private void refreshBattleScreen() {
        Player p1 = gc.getPlayer1();
        Player p2 = gc.getPlayer2();

        p1NameLabel.setText("Player 1 – " + p1.getName() + " (" + p1.getClass().getSimpleName() + ")");
        p2NameLabel.setText("Player 2 – " + p2.getName() + " (" + p2.getClass().getSimpleName() + ")");

        updateStatBars(p1, p2);
        battleLog.setText("⚔  Battle begins! Player 1 goes first.\n\n");
        refreshActionPanel();
    }

    /** Syncs all HP/mana labels and bars to current game state. */
    private void updateStatBars(Player p1, Player p2) {
        p1HpLabel.setText(String.format("HP:   %d / %d", p1.getHealth(), GameConstants.MAX_HEALTH));
        p1HpBar.setValue(p1.getHealth());
        p1MpLabel.setText(String.format("Mana: %d / %d", p1.getMana(),   GameConstants.MAX_MANA));
        p1MpBar.setValue(p1.getMana());

        p2HpLabel.setText(String.format("HP:   %d / %d", p2.getHealth(), GameConstants.MAX_HEALTH));
        p2HpBar.setValue(p2.getHealth());
        p2MpLabel.setText(String.format("Mana: %d / %d", p2.getMana(),   GameConstants.MAX_MANA));
        p2MpBar.setValue(p2.getMana());
    }

    /** Rebuilds the action buttons for the current attacker. */
    private void refreshActionPanel() {
        actionPanel.removeAll();

        Player attacker = gc.getCurrentAttacker();
        boolean isP1    = (attacker == gc.getPlayer1());
        String  whoStr  = isP1 ? "Player 1" : "Player 2";
        turnLabel.setText("⚔  " + whoStr + "'s Turn  (" + attacker.getClass().getSimpleName() + ")");
        turnLabel.setForeground(isP1 ? ACCENT_GOLD : ACCENT_BLUE);

        // ── Attack ──
        JButton attackBtn = makeButton("⚔ Attack", ACCENT_RED, BG_DARK);
        attackBtn.addActionListener(e -> doAction("attack", 0));
        actionPanel.add(attackBtn);

        // ── Skills (from the player's skill list) ──
        List<Skill> skills = attacker.getSkills();
        for (int i = 0; i < skills.size(); i++) {
            final int idx = i;
            Skill s = skills.get(i);
            JButton sb = makeButton("✨ " + s.getName()
                    + " (" + s.getManaCost() + " MP)", ACCENT_BLUE, BG_DARK);
            sb.addActionListener(e -> doAction("skill", idx));
            actionPanel.add(sb);
        }

        // ── Inventory items ──
        List<Item> inv = attacker.getInventory().getItems();
        for (int i = 0; i < inv.size(); i++) {
            final int idx = i;
            Item item = inv.get(i);
            JButton ib = makeButton("🎒 " + item.getName(), ACCENT_GREEN, BG_DARK);
            ib.addActionListener(e -> doAction("useItem", idx));
            actionPanel.add(ib);
        }

        // ── Combo (only if matching pair exists) ──
        if (attacker.getInventory().hasMatchingPotions(2)) {
            JButton comboBtn = makeButton("💥 COMBO", ACCENT_GOLD, BG_DARK);
            comboBtn.addActionListener(e -> doAction("combo", 0));
            actionPanel.add(comboBtn);
        }

        actionPanel.revalidate();
        actionPanel.repaint();
    }

    /** Dispatches a battle action, logs the result, and checks for game over. */
    private void doAction(String type, int param) {
        am.aua.dungeonparty.core.TurnResult res = gc.processBattleAction(type, param);
        battleLog.append("  " + res.getMessage() + "\n");

        // Auto-scroll to bottom
        battleLog.setCaretPosition(battleLog.getDocument().getLength());

        updateStatBars(gc.getPlayer1(), gc.getPlayer2());

        if (gc.isBattleOver()) {
            showEndScreen();
        } else {
            refreshActionPanel();
        }
    }

    /** Saves the current game state to "savegame.csv". */
    private void saveCurrentGame() {
        try {
            SaveLoadManager.saveGame(gc, "savegame.csv");
            battleLog.append("  💾 Game saved successfully.\n");
        } catch (SaveFileException ex) {
            battleLog.append("  ❌ Save failed: " + ex.getMessage() + "\n");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  END SCREEN
    // ═════════════════════════════════════════════════════════════════════════

    private JLabel endWinnerLabel;
    private JLabel endQuoteLabel;

    private JPanel buildEndScreen() {
        JPanel panel = new GradientPanel(BG_DARK, BG_MID) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Gold confetti dots
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 0; i < 40; i++) {
                    int x = (i * 73 + 30) % getWidth();
                    int y = (i * 57 + 20) % getHeight();
                    g2.setColor(new Color(255, 200, 50, 120));
                    g2.fillOval(x, y, 8, 8);
                }
            }
        };
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 20, 12, 20);
        gbc.gridx  = 0;

        // Trophy
        JLabel trophy = new JLabel("🏆", SwingConstants.CENTER);
        trophy.setFont(new Font("SansSerif", Font.PLAIN, 72));
        gbc.gridy = 0;
        panel.add(trophy, gbc);

        // Winner name
        endWinnerLabel = new JLabel("Player X wins!", SwingConstants.CENTER);
        endWinnerLabel.setFont(TITLE_FONT);
        endWinnerLabel.setForeground(ACCENT_GOLD);
        gbc.gridy = 1;
        panel.add(endWinnerLabel, gbc);

        // Fun quote shown over the winner's "head"
        endQuoteLabel = new JLabel("haha I won 😂", SwingConstants.CENTER);
        endQuoteLabel.setFont(new Font("Serif", Font.ITALIC, 22));
        endQuoteLabel.setForeground(ACCENT_GREEN);
        gbc.gridy = 2;
        panel.add(endQuoteLabel, gbc);

        // Play Again
        JButton playAgainBtn = makeButton("🔄  PLAY AGAIN", ACCENT_GOLD, BG_DARK);
        playAgainBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
        playAgainBtn.addActionListener(e -> {
            // Reset and go back to character picking
            gc = new GameController();
            gc.setStore(new Store());
            currentPickingPlayer = 1;
            refreshPickScreen();
            cardLayout.show(rootPanel, CARD_PICK);
        });
        gbc.gridy = 3;
        gbc.insets = new Insets(20, 20, 8, 20);
        panel.add(playAgainBtn, gbc);

        // Main Menu
        JButton menuBtn = makeButton("🏠  Main Menu", TEXT_LIGHT, BG_MID);
        menuBtn.addActionListener(e -> cardLayout.show(rootPanel, CARD_WELCOME));
        gbc.gridy = 4;
        gbc.insets = new Insets(4, 20, 12, 20);
        panel.add(menuBtn, gbc);

        return panel;
    }

    /** Populates the end screen with the winner's info and switches to it. */
    private void showEndScreen() {
        Player winner = gc.getWinner();
        if (winner == null) return;

        boolean isP1  = (winner == gc.getPlayer1());
        String whoStr = isP1 ? "Player 1" : "Player 2";

        endWinnerLabel.setText("🏆  " + whoStr + " (" + winner.getClass().getSimpleName() + ")  Wins!");
        endWinnerLabel.setForeground(isP1 ? ACCENT_GOLD : ACCENT_BLUE);
        endQuoteLabel.setText("\"haha I won 😂\"  — " + whoStr);

        cardLayout.show(rootPanel, CARD_END);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═════════════════════════════════════════════════════════════════════════

    /** Creates a styled button with rounded corners and hover effect. */
    private JButton makeButton(String text, Color fg, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(fg.darker());
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(fg);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 14, 14);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(fg);
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setFont(BTN_FONT);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(Math.max(btn.getPreferredSize().width + 20, 140), 36));
        return btn;
    }

    /** Small utility for JOptionPane messages. */
    private void showMsg(String msg, String title) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.WARNING_MESSAGE);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  INNER CLASSES
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * GradientPanel – a JPanel with a top-to-bottom gradient background.
     * Used as the base for every screen so we don't paint raw grey.
     */
    static class GradientPanel extends JPanel {
        private final Color top, bottom;
        GradientPanel(Color top, Color bottom) {
            this.top = top; this.bottom = bottom;
            setOpaque(true);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    /**
     * CharArtPanel – draws a simple geometric "sprite" for each character class.
     * No external image files needed; everything is Java2D shapes.
     */
    static class CharArtPanel extends JPanel {
        private final String charName;
        private final Color  accent;
        private boolean highlighted = false;

        CharArtPanel(String charName, Color accent) {
            this.charName = charName;
            this.accent   = accent;
            setOpaque(false);
        }

        void setHighlighted(boolean h) { this.highlighted = h; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int cx = w / 2;

            // Glow on hover
            if (highlighted) {
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60));
                g2.fillOval(cx - 45, h / 2 - 45, 90, 90);
            }

            // Body colour
            Color skin  = new Color(220, 180, 140);
            Color dark  = accent.darker();

            // Head
            g2.setColor(skin);
            g2.fillOval(cx - 18, 10, 36, 36);
            g2.setColor(dark);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(cx - 18, 10, 36, 36);

            // Body / torso (different shapes per class)
            switch (charName) {
                case "Warrior":
                    // Heavy armour plate
                    g2.setColor(accent);
                    g2.fillRect(cx - 20, 48, 40, 45);
                    g2.setColor(dark);
                    g2.drawRect(cx - 20, 48, 40, 45);
                    // Sword
                    g2.setColor(new Color(200, 200, 220));
                    g2.fillRect(cx + 22, 38, 5, 55);
                    g2.setColor(ACCENT_GOLD);
                    g2.fillRect(cx + 16, 60, 18, 4);
                    break;

                case "Mage":
                    // Robe
                    int[] robeX = { cx - 20, cx + 20, cx + 26, cx - 26 };
                    int[] robeY = { 48, 48, 100, 100 };
                    g2.setColor(accent);
                    g2.fillPolygon(robeX, robeY, 4);
                    g2.setColor(dark);
                    g2.drawPolygon(robeX, robeY, 4);
                    // Staff
                    g2.setColor(new Color(140, 90, 40));
                    g2.fillRect(cx + 22, 20, 4, 80);
                    g2.setColor(new Color(100, 200, 255));
                    g2.fillOval(cx + 16, 12, 18, 18);
                    break;

                case "Healer":
                    // White robe with cross
                    g2.setColor(new Color(240, 240, 255));
                    g2.fillRoundRect(cx - 18, 48, 36, 48, 8, 8);
                    g2.setColor(accent);
                    g2.setStroke(new BasicStroke(4f));
                    g2.drawLine(cx, 56, cx, 80);
                    g2.drawLine(cx - 10, 66, cx + 10, 66);
                    g2.setColor(dark);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(cx - 18, 48, 36, 48, 8, 8);
                    break;

                case "Ranger":
                    // Leather torso
                    g2.setColor(accent);
                    g2.fillRoundRect(cx - 16, 48, 32, 42, 6, 6);
                    // Bow
                    g2.setColor(new Color(140, 90, 40));
                    g2.setStroke(new BasicStroke(3f));
                    g2.drawArc(cx + 20, 25, 20, 60, -90, 180);
                    g2.setColor(new Color(200, 200, 150));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawLine(cx + 20, 25, cx + 20, 85);
                    break;

                case "Rogue":
                    // Dark cloak
                    int[] cloakX = { cx - 22, cx + 22, cx + 18, cx - 18 };
                    int[] cloakY = { 48, 48, 98, 98 };
                    g2.setColor(accent);
                    g2.fillPolygon(cloakX, cloakY, 4);
                    // Dagger
                    g2.setColor(new Color(180, 180, 200));
                    g2.fillRect(cx - 28, 50, 4, 35);
                    g2.setColor(ACCENT_GOLD);
                    g2.fillRect(cx - 32, 68, 12, 3);
                    break;

                default:
                    // Fallback: plain body
                    g2.setColor(accent);
                    g2.fillRect(cx - 16, 48, 32, 44);
                    break;
            }

            // Legs
            g2.setColor(dark);
            g2.setStroke(new BasicStroke(1f));
            g2.fillRect(cx - 14, 93, 10, 20);
            g2.fillRect(cx + 4, 93, 10, 20);

            // Eyes
            g2.setColor(Color.BLACK);
            g2.fillOval(cx - 10, 20, 5, 5);
            g2.fillOval(cx + 5, 20, 5, 5);

            // Smile
            g2.setColor(dark);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawArc(cx - 8, 30, 16, 10, 200, 140);
        }
    }
}