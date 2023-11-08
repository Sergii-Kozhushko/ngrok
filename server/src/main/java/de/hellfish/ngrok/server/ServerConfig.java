package de.hellfish.ngrok.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class ServerConfig {

    @Bean
    public Map<String, Socket> clientConnections() {
        return new HashMap<>();
    }
}

