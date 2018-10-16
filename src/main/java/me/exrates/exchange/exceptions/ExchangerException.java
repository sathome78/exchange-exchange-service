package me.exrates.exchange.exceptions;

public class ExchangerException extends RuntimeException {

    public ExchangerException() {
        super();
    }

    public ExchangerException(String message) {
        super(message);
    }

    public ExchangerException(String message, Throwable cause) {
        super(message, cause);
    }
}
