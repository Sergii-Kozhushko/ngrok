package de.hellfish.ngrok.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final ClientConnectionService clientList;

    @Override
    public void run() {
        log.info("New client connected: {}:{}", clientSocket.getLocalAddress(), clientSocket.getLocalAddress());
        Optional<ClientInitRequest> protocolAndPort = fetchServiceProtocolAndPort(clientSocket);
        if (protocolAndPort.isEmpty()) {
            log.error("Error on handshake with client");
            return;
        }

        if (!protocolAndPort.get().getProtocol().equalsIgnoreCase("HTTP")) {
            String errorMessage = "Protocol '" + protocolAndPort.get().getValue() + "' is not supported by server";
            log.error(errorMessage);
            sendMessageToClient("ERROR " + errorMessage, clientSocket);
        } else {
            // TODO add user-link generation
            String generatedLink = "http://sub1.localhost:9000";
            clientList.addClient(generatedLink, clientSocket);
            log.info("Received request from client (%{}:%{}): %{}", clientSocket.getLocalAddress(),
                    clientSocket.getPort(), protocolAndPort.get().getValue());
            sendMessageToClient("LINK " + generatedLink, clientSocket);
        }
    }

    private void sendMessageToClient(String message, Socket clientSocket) {
        try {
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
            pw.write(message + "\n");
            pw.flush();
        } catch (IOException e) {
            throw new NoConnectionToClientException("Error connection to client", e);
        }
    }

    private Optional<ClientInitRequest> fetchServiceProtocolAndPort(Socket clientSocket) {
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