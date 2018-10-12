package me.exrates.exchange.exceptions;

public class NoSuchExchangerException extends RuntimeException {

    public NoSuchExchangerException() {
        super();
    }

    public NoSuchExchangerException(String message) {
        super(message);
    }

    public NoSuchExchangerException(String message, Throwable cause) {
        super(message, cause);
    }
}
