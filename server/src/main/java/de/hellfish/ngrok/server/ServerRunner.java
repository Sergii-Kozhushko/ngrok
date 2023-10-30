package de.hellfish.ngrok.server;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class ServerRunner implements CommandLineRunner {

    private static final int clientToServerPort = 8082;
    private boolean isRunning = true;

    private final Map<ClientMethodAndPort, Socket> clientConnections = new HashMap<>();
    private final Map<Integer, Socket> userConnections = new HashMap<>();


    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0) {
            log.info("first command-line parameter: '{" + args[0] + "}'" );
        }

        try (ServerSocket clientServerSocket = new ServerSocket(clientToServerPort);
        ) {
            log.info("Server is listening on clients on port: " + clientToServerPort);
            Socket clientSocket;
            while (isRunning) {

                clientSocket = clientServerSocket.accept();
                // new client!
                log.info("New client connected from " + clientSocket.getLocalAddress() + ":" + clientSocket.getPort());

                Optional<ClientMethodAndPort> methodAndPort = getServicePortAndMethodFromClient(clientSocket);
                if (methodAndPort.isPresent()) {
                    if (clientConnections.containsKey(methodAndPort.get())) {
                        String errorMessage = "ERROR " + methodAndPort.get() + " is already in use";
                        log.warn(errorMessage);
                        sendClientMessage(errorMessage, clientSocket);
                    } else {
                        clientConnections.put(methodAndPort.get(), clientSocket);
                        log.info("New client successfully connected with proxy request " + methodAndPort.get());

                        int tempUserRequestsPort = 9001;
                        String generatedLink = "http://localhost:" + tempUserRequestsPort;
                        sendClientMessage("LINK " + generatedLink, clientSocket);
                        UserHttpServer userHttpServer =
                                new UserHttpServer(tempUserRequestsPort, clientSocket, userConnections);

                        userHttpServer.startServer();
                        log.info("Server is listening on users on: " + methodAndPort.get());
                    }
                } else {
                    log.info("Received wrong protocol and port value");
                    sendClientMessage("ERROR Didn't receive service protocol and port", clientSocket);
                }
            }
        } catch (IOException e) {
            log.error("Error opening server socket", e);
        }
    }

    private void sendClientMessage(String message, Socket clientSocket) {
        try {
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
            pw.write(message + "\n");
            pw.flush();
        } catch (IOException e) {
            log.error("Error sending message to Client", e);
        }
    }

    private Optional<ClientMethodAndPort> getServicePortAndMethodFromClient(Socket clientSocket) {
        try {
            BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String clientRequest = clientReader.readLine();
            String[] requestParts = clientRequest.split(" ");
            String method = requestParts[0].toUpperCase();
            String port = requestParts[1];

            if (!method.equals("HTTP") || !port.matches("^[1-9]\\d*$")) {
                return Optional.empty();
            } else {
                ClientMethodAndPort mp = new ClientMethodAndPort(Integer.valueOf(port), method);
                return Optional.of(mp);
            }
        } catch (IOException e) {
            log.error("Error reading init data from client", e);
        }
        return Optional.empty();
    }
}