package com.example.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@Component
public class MyServerRunner implements CommandLineRunner {
    private static final int userToServerPort = 8081;


    private static final int clientToServerPort = 8082;


    private boolean isRunning = true;

    @Autowired
    public static final Logger logger = LoggerFactory.getLogger(MyServerRunner.class);

    // какие клиенты какие порты заняли
    private Map<ClientMethodAndPort, Socket> clientConnections = new HashMap<>();

    // список входящих соединений от юзеров
    private Map<Integer, Socket> userConnections = new HashMap<>();

    private Map<Integer, String> links = new HashMap<>();

    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0) {
            logger.info("first command-line parameter: '{}'", args[0]);
        }

        // слушатель запросов всех клиентов на 8082
        try (ServerSocket clientServerSocket = new ServerSocket(clientToServerPort);
        ) {
            logger.info("Server is listening on clients on port: " + clientToServerPort);
            Socket clientSocket;
            while (isRunning) {

                clientSocket = clientServerSocket.accept();
                // появился новый клиент!
                logger.info("New client connected from " + clientSocket.getLocalAddress() + ":" + clientSocket.getPort());

                // при установке соединения клиент передает строку вида HTTP 2222, с номером порта локального сервиса,
                // который надо проксить
                Optional<ClientMethodAndPort> methodAndPort = getServicePortAndMethodFromClient(clientSocket);
                // добавить нового клиента в мапу клиентов
                if (methodAndPort.isPresent()) {
                    if (clientConnections.containsKey(methodAndPort.get())) {
                        String errorMessage = "ERROR " + methodAndPort.get() + " is already in use";
                        logger.warn(errorMessage);
                        sendClientMessage(errorMessage, clientSocket);
                    } else {

                        //ClientHandler clientHandler = new ClientHandler(clientSocket, userConnections);
                        clientConnections.put(methodAndPort.get(), clientSocket);
                        //clientHandler.start();
                        logger.info("New client successfully connected with proxy request " + methodAndPort.get());

                        // TODO сгенерировать ссылку с субдоменом для запросов юзера типа http://tcp1.myngrok.com
                        // пока просто берем какой то порт 9001..9010 и генерим ссылку типа http://localhost:9001
                        // TODO сделать метод, который выбирает из пула из свободных портов
                        int tempUserRequestsPort = 9001;
                        String generatedLink = "http://localhost:" + tempUserRequestsPort;
                        sendClientMessage("LINK " + generatedLink, clientSocket);
                        // где-то надо сохранить что для запросов сервиса 2222 отрабатывают запросы юзеров 9001
                        links.put(methodAndPort.get().getPort(), generatedLink);

                        // стартануть поток для работы с запросами юзеров на выданном порту 9001
                        UserHttpServer userHttpServer =
                                new UserHttpServer(tempUserRequestsPort, clientSocket, userConnections);

                        userHttpServer.startServer();

                        logger.info("Server is listening on users on: " + methodAndPort.get());
                    }


                } else {
                    logger.info("Received wrong protocol and port value");
                    sendClientMessage("ERROR Didn't receive service protocol and port", clientSocket);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendClientMessage(String message, Socket clientSocket) {
        try {
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
            pw.write(message + "\n");
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
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
            new RuntimeException(e);
        }
        return Optional.empty();
    }

}
