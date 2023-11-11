package de.hellfish.ngrok.server;

import java.net.Socket;

public interface ClientConnectionService {
    void addClient(String key, Socket socket);
    Socket getClient(String key);
    void removeClient(String key);
}