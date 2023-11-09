package de.hellfish.ngrok.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServerRunner implements CommandLineRunner {

    @Value("${client.port}")
    private int clientPort;
    private final Map<String, Socket> clientConnections;
    private final ExecutorService executors = Executors.newFixedThreadPool(5);
    private final AtomicBoolean running = new AtomicBoolean(true);
    private ServerSocket serverSocket;

    @Override
    public void run(String... args) {
        try  {
            serverSocket = new ServerSocket(clientPort);
            log.info(String.format("Ngrok-Server started. Listening on clients on port: %s", clientPort));
            Socket clientSocket;
            while (running.get()) {
                clientSocket = serverSocket.accept();
                executors.execute(new ClientHandler(clientSocket, clientConnections));
            }
        } catch (IOException e) {
            log.error(String.format("Error opening server socket, probably port %s is busy", clientPort), e);
        } finally {
            stopServer();
        }
    }

    public void stopServer() {
        running.set(false);
        executors.shutdownNow();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log.error("Could not close server socket", e);
        }
    }

    @RequiredArgsConstructor
    static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final Map<String, Socket> clientList;

        @Override
        public void run() {
            log.info(String.format("New client connected: %s:%s", clientSocket.getLocalAddress(), clientSocket.getLocalAddress()));
            Optional<ClientInitRequest> protocolAndPort = fetchServiceProtocolAndPort(clientSocket);
            if (protocolAndPort.isEmpty()) {
                log.error("Error on handshake with client");
                return;
            }

            if (!protocolAndPort.get().getProtocol().equalsIgnoreCase("HTTP")) {
                String errorMessage = String.format("ERROR Protocol '%s' is not supported by server", protocolAndPort.get().getValue());
                log.error(errorMessage);
                sendMessageToClient(errorMessage, clientSocket);
            } else {
                // TODO add user-link generation
                String generatedLink = "http://sub1.localhost:9000";
                clientList.put(generatedLink, clientSocket);
                log.info(String.format("Received request from client (%s:%s): %s", clientSocket.getLocalAddress(),
                        clientSocket.getPort(), protocolAndPort.get().getValue()));
                sendMessageToClient("LINK " + generatedLink, clientSocket);
            }
        }
    }

    private static void sendMessageToClient(String message, Socket clientSocket) {
        try {
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
            pw.write(message + "\n");
            pw.flush();
        } catch (IOException e) {
            throw new NoConnectionToClientException("Error connection to client", e);
        }
    }

    private static Optional<ClientInitRequest> fetchServiceProtocolAndPort(Socket clientSocket) {
        try {
            BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String clientRequest = clientReader.readLine();
            String[] requestParts = clientRequest.split(" ");
            String protocol = requestParts[0].toUpperCase();
            String port = requestParts[1];

            if (!protocol.equals("HTTP") || !port.matches("^[1-9]\\d*$")) {
                log.error("Init request from client has irregular format");
                return Optional.empty();
            } else {
                ClientInitRequest mp = new ClientInitRequest(Integer.parseInt(port), protocol);
                return Optional.of(mp);
            }
        } catch (IOException e) {
            log.error("Error connection to client", e);
        }
        return Optional.empty();
    }
}