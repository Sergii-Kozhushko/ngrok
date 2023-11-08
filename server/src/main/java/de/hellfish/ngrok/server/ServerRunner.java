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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServerRunner implements CommandLineRunner {

    private static final int CLIENT_PORT = 8082;
    private final Map<String, Socket> clientConnections;
    private final ExecutorService executors = Executors.newFixedThreadPool(5);

    @Override
    public void run(String... args) {

        try (ServerSocket clientServerSocket = new ServerSocket(CLIENT_PORT)) {
            log.info(String.format(Messages.SERVER_START, CLIENT_PORT));
            Socket clientSocket;
            while (true) {
                clientSocket = clientServerSocket.accept();
                executors.execute(new ClientHandler(clientSocket, clientConnections));
            }
        } catch (IOException e) {
            log.error(String.format(Messages.ERROR_OPEN_SERVER_SOCKET, CLIENT_PORT), e);
        }
    }

    @RequiredArgsConstructor
    static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final Map<String, Socket> clientList;

        @Override
        public void run() {
            log.info(String.format(Messages.NEW_CLIENT, clientSocket.getLocalAddress(), clientSocket.getLocalAddress()));
            Optional<ClientInitRequest> protocolAndPort = fetchServiceProtocolAndPort(clientSocket);
            if (protocolAndPort.isEmpty()) {
                log.error(Messages.ERROR_HANDSHAKE);
                return;
            }

            if (!protocolAndPort.get().getProtocol().equalsIgnoreCase("HTTP")) {
                String errorMessage = String.format(Messages.ERROR_WRONG_PROTOCOL, protocolAndPort.get().getValue());
                log.error(errorMessage);
                sendMessageToClient(errorMessage, clientSocket);
            } else {
                // TODO add user-link generation
                String generatedLink = "http://sub1.localhost:9000";
                clientList.put(generatedLink, clientSocket);
                log.info(String.format(Messages.NEW_INIT_REQUEST_FROM_CLIENT, clientSocket.getLocalAddress(),
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
            throw new NoConnectionToClientException(Messages.ERROR_CONNECTION_CLIENT, e);
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
                log.error(Messages.ERROR_INIT_REQUEST_CLIENT);
                return Optional.empty();
            } else {
                ClientInitRequest mp = new ClientInitRequest(Integer.parseInt(port), protocol);
                return Optional.of(mp);
            }
        } catch (IOException e) {
            log.error(Messages.ERROR_CONNECTION_CLIENT, e);
        }
        return Optional.empty();
    }
}