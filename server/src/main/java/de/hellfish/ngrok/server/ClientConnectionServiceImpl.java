package de.hellfish.ngrok.server;

import org.springframework.stereotype.Service;

import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ClientConnectionServiceImpl implements ClientConnectionService{
    private final ConcurrentHashMap<String, Socket> clientConnections = new ConcurrentHashMap<>();

    @Override
    public void addClient(String key, Socket socket) {
        clientConnections.put(key, socket);
    }

    @Override
    public Socket getClient(String key) {
        return clientConnections.get(key);
    }

    @Override
    public void removeClient(String key) {
        clientConnections.remove(key);
    }

    @Override
    public boolean isEmpty() {
        return clientConnections.isEmpty();
    }
}