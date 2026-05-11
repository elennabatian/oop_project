package am.aua.dungeonparty.core;

// Stores the outcome of a single combat action.

public class TurnResult {
    private boolean success;
    private String message;
    private int damageDone;
    private int coinsChanged;
    private boolean extraTurn;
    private boolean opponentStunned;

    public TurnResult(boolean success, String message, int damageDone, int coinsChanged) {
        this(success, message, damageDone, coinsChanged, false, false);
    }

    public TurnResult(boolean success, String message, int damageDone, int coinsChanged,
                      boolean extraTurn, boolean opponentStunned) {
        this.success = success;
        this.message = message;
        this.damageDone = damageDone;
        this.coinsChanged = coinsChanged;
        this.extraTurn = extraTurn;
        this.opponentStunned = opponentStunned;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public int getDamageDone() { return damageDone; }
    public boolean grantsExtraTurn() { return extraTurn; }
    public boolean isOpponentStunned() { return opponentStunned; }
}