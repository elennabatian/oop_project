package am.aua.dungeonparty.exceptions;

public class SaveFileException extends Exception {
    public SaveFileException(String message) {
        super(message);
    }

    public SaveFileException(String message, Throwable cause) {
        super(message, cause);
    }
}