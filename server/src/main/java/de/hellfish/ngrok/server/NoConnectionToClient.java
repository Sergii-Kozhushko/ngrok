package de.hellfish.ngrok.server;

public class NoConnectionToClient extends RuntimeException {
    private final String message;

    public NoConnectionToClient(String message) {
        super(message);
        this.message = message;
    }

}