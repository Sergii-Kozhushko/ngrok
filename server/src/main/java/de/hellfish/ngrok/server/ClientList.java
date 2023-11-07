package de.hellfish.ngrok.server;

import lombok.Getter;
import org.springframework.stereotype.Component;
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

}