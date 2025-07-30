package ua.hudyma.exception;

public class NoMainPassengerBookingException extends RuntimeException{
    public NoMainPassengerBookingException(String message) {
        super(message);
    }
}
