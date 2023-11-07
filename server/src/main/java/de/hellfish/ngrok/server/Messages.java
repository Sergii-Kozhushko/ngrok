package de.hellfish.ngrok.server;

public class Messages {
    public static final String SERVER_START = "Ngrok-Server started. Listening on clients on port: %s";
    public static final String ERROR_OPEN_SERVER_SOCKET = "Error opening server socket, probably port %s is busy";
    public static final String ERROR_HANDSHAKE = "Error on handshake with client";
    public static final String ERROR_WRONG_PROTOCOL = "ERROR Protocol '%s' is not supported by server";
    public static final String ERROR_CONNECTION_CLIENT = "Error connection to client";
    public static final String ERROR_INIT_REQUEST_CLIENT = "Init request from client has irregular format";
    public static final String ERROR_WRONG_LINK_USER = "Link %s was not recognized by ngrok-server";
    public static final String NEW_CLIENT = "New client connected: %s:%s";
    public static  final String NEW_INIT_REQUEST_FROM_CLIENT = "Received request from client (%s:%s): %s";
    public static  final String NEW_USER_REQUEST = "Server received user request: %s";



}
