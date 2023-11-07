package de.hellfish.ngrok.server;

import lombok.Getter;
import org.springframework.stereotype.Component;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores client data: socket connection, link for users and service port.
 * Example: http://localhost:2222 -> http://sub1.localhost:9000
 */
@Getter
@Component
public class ClientList {
    private final Map<String, SocketState> list = new HashMap<>();

    public boolean containsProtocolAndPort(ClientInitRequest clientRequest, Socket clientSocket) {
        for (Map.Entry<String, SocketState> entry : list.entrySet()) {
            SocketState value = entry.getValue();
            if (value.getServicePort() == clientRequest.getPort() && value.getServiceProtocol().equals(clientRequest.getProtocol())
            && clientSocket.getLocalAddress().toString().equals(value.getSocket().getLocalAddress().toString())) {
                return true;
            }
        }
        return false;
    }
}