package de.hellfish.ngrok.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Getter
@Setter
@Component
@RequiredArgsConstructor
public class Server implements Runnable {
    @Value("${client.port}")
    private int clientPort;
    private final Map<String, Socket> clientConnections;
    private ServerSocket serverSocket;
    private boolean running;
    private final ExecutorService executors = Executors.newFixedThreadPool(5);

    public void startServer() {
        try {
            serverSocket = new ServerSocket(clientPort);
            running = true;
            log.info("Ngrok-Server started. Listening on clients on port: {}", clientPort);
        } catch (
                IOException e) {
            log.error("Error opening server socket, probably port {} is down", clientPort, e);
        }
    }

    public void stopServer() {
        running = false;
        executors.shutdownNow();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log.error("Could not close server socket", e);
        }
    }

    @Override
    public void run() {
        startServer();
        while (running) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                log.error("Error opening server socket, probably port {} is down", clientPort, e);
            }
            executors.execute(new ClientHandler(clientSocket, clientConnections));
        }
        stopServer();
    }
}