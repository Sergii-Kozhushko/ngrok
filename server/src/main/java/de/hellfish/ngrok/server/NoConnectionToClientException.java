package de.hellfish.ngrok.server;

public class NoConnectionToClientException extends RuntimeException {
    private final String message;

    public NoConnectionToClientException(String message) {
        super(message);
        this.message = message;
    }
}