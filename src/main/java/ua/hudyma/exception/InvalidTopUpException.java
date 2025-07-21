package ua.hudyma.exception;

public class InvalidTopUpException extends RuntimeException {
    public InvalidTopUpException(String message) {
        super(message);
    }

    public InvalidTopUpException(Exception e) {
    }
}
