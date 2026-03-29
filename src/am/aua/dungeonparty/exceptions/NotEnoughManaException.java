package am.aua.dungeonparty.exceptions;

public class NotEnoughManaException extends RuntimeException {
    public NotEnoughManaException(String message) {
        super(message);
    }
}
