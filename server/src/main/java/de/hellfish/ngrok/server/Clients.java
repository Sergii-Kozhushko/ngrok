package de.hellfish.ngrok.server;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

@Getter
@Component
public class Clients {
    private Map<String, Socket> list = new HashMap<>();

}
