package de.hellfish.ngrok.server;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * Stores client data: socket connection, link for users and service port.
 * Example: http://localhost:2222 -> http://sub1.localhost:9000
 */
@Getter
@Component
public class ClientList {
    private final Set<ClientData> list = new HashSet<>();

    public boolean containsProtocolAndPort(String pp) {
        for (ClientData cd : list) {
            if (cd.getServiceProtocolAndPort().equals(pp)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsUserLink(String userLink) {
        for (ClientData cd : list) {
            if (cd.getUserLink().equals(userLink)) {
                return true;
            }
        }
        return false;
    }

    public Socket getSocketByUserLink(String userLink) {
        for (ClientData cd : list) {
            if (cd.getUserLink().equals(userLink)) {
                return cd.getSocket();
            }
        }
        return null;
    }

    public void add(ClientData cd) {
        list.add(cd);
    }
}