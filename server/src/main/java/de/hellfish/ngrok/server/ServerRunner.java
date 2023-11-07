package de.hellfish.ngrok.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServerRunner implements CommandLineRunner {

    private static final int clientToServerPort = 8082;
    private boolean isRunning = true;
    private final ClientList clientConnections;
    ExecutorService executors = Executors.newFixedThreadPool(5);

    @Override
    public void run(String... args) {

        try (ServerSocket clientServerSocket = new ServerSocket(clientToServerPort)) {
            log.info("Ngrok-Server started. Listening on clients on port: " + clientToServerPort);
            Socket clientSocket;
            while (isRunning) {
                clientSocket = clientServerSocket.accept();
                executors.execute(new ClientHandler(clientSocket, clientConnections));
            }
        } catch (IOException e) {
            log.error("Error opening server socket", e);
        }
    }

    @RequiredArgsConstructor
    static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final ClientList clientList;

        @Override
        public void run() {
            log.info("New client connected: " + clientSocket.getLocalAddress() + ":" + clientSocket.getPort());
            Optional<ClientInitRequest> protocolAndPort = fetchServiceProtocolAndPort(clientSocket);
            if (protocolAndPort.isEmpty()) {
                log.error("Error on handshake with client");
                return;
            }

            if (clientList.containsProtocolAndPort(protocolAndPort.get(), clientSocket)) {
                String errorMessage = "ERROR " + protocolAndPort.get().getValue() + " is already in use";
                log.error(errorMessage);
                sendMessageToClient(errorMessage, clientSocket);
            } else {
                // TODO add user-link generation
                String generatedLink = "http://sub1.localhost:9000";
                SocketState clientData = new SocketState(clientSocket, protocolAndPort.get().getProtocol(), protocolAndPort.get().getPort());
                clientList.getList().put(generatedLink, clientData);
                log.info("Received proxy request from client (" + clientSocket.getLocalAddress() + ":" +
                                clientSocket.getPort() + "): " + protocolAndPort.get().getValue());
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
            throw new NoConnectionToClientException("Error sending service message to client: " + message);
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
            log.error("Error reading init request from client", e);
        }
        return Optional.empty();
    }
}