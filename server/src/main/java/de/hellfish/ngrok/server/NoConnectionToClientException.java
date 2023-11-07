package de.hellfish.ngrok.server;

public class NoConnectionToClientException extends RuntimeException {

    public NoConnectionToClientException(String message, Throwable cause) {
        super(message, cause);
    }
}