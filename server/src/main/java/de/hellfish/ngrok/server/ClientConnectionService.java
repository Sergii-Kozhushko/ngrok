package de.hellfish.ngrok.server;

import org.springframework.stereotype.Service;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ClientConnectionService {

    private final ConcurrentHashMap<String, Socket> clientConnections = new ConcurrentHashMap<>();

    public void addClient(String key, Socket socket) {
        clientConnections.put(key, socket);
    }

    public Socket getClient(String key) {
        return clientConnections.get(key);
    }

    public void removeClient(String key) {
        clientConnections.remove(key);
    }

    public boolean isEmpty() {
        return clientConnections.isEmpty();
    }
}