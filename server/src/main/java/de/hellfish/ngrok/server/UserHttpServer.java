package de.hellfish.ngrok.server;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Getter
@Setter
public class UserHttpServer extends Thread {
    private int userPort;
    private Socket clientSocket; //соединение клиента, которое инициировало принимать запросы  юзеров на этот порт
    private boolean running = true;
    private ServerSocket userServerSocket;
    private Map<Integer, Socket> userConnections; // список юзеров, которые установили соединение
    ExecutorService executors = Executors.newFixedThreadPool(5);

    public UserHttpServer(int userPort, Socket clientSocket, Map<Integer, Socket> userConnections) {
        this.userPort = userPort;
        this.clientSocket = clientSocket;
        this.userConnections = userConnections;
    }

    public void startServer() {
        try {
            userServerSocket = new ServerSocket(userPort);
            this.start();
        } catch (IOException e) {
            log.error(userPort + " is busy. Users Server can't run", e);
        }
    }

    public void stopServer() {
        running = false;
        this.interrupt();
    }

    @Override
    public void run() {
        try {
            // Socket userSocket;
            while (running) {
                var userSocket = userServerSocket.accept();
                userConnections.put(userSocket.getPort(), userSocket);
                log.info("New user connected from " + userSocket.getLocalAddress() + ":" + userSocket.getPort());

                executors.submit(() -> {
                    try {
                        BufferedReader userInputStream = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
                        String line;
                        StringBuilder userRequest = new StringBuilder();
                        int contentLength = 0;
                        line = userInputStream.readLine();
                        while (line  != null && !line.isEmpty()) {
                            userRequest.append(line).append("\r\n");
                            if (line.contains("Content-Length")) {
                                try {
                                    contentLength = Integer.parseInt(line.split(" ")[1]);
                                } catch (NumberFormatException ignored) {

                                }
                            }
                            line = userInputStream.readLine();
                        }

                        // read body
                        if (contentLength > 0) {
                            //readBody(contentLength, userSocket);

                        }



                        if (userRequest.length() == 0) {
                            this.interrupt();
                        }
                        log.info("User handler got request from " +
                                userSocket.getLocalAddress() + ":" + userSocket.getPort() + "\n" + "*" + userRequest + "*");

                        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                        writer.write("USER " + userSocket.getPort() + "\r\n" + userRequest);
                        writer.write("\n");

                        writer.flush();

                        // reda response from clinet and send to user
                        BufferedReader clientReadStream =
                                new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        StringBuilder clientResponse = new StringBuilder();
                        while ((line = clientReadStream.readLine()) != null && !line.isEmpty()) {
                            clientResponse.append(line).append("\r\n");
                        }
                        line = clientReadStream.readLine();
                        if (!line.isEmpty()) clientResponse.append("\r\n" + line);
                        int userNumber = Integer.parseInt(clientResponse.toString().split("\r\n")[0].split(" ")[1]);
                        String clientResponsePure =
                                clientResponse.toString().substring(("USER " + userNumber + "\r\n").length());
                        PrintWriter userWriteStream = new PrintWriter(userSocket.getOutputStream());

                        userWriteStream.write(clientResponsePure);
                        userWriteStream.flush();
                        userWriteStream.close();
                    } catch (IOException e) {
                        log.error("Socket error on server", e);
                    }
                });
            }
        } catch (IOException e) {
            log.error("Socket error on server", e);
        }
    }

    private String readBody(int contentLength, Socket socket) {
        byte[] buffer = new byte[contentLength];
        try {
            InputStream inputStream = socket.getInputStream();

            int n = 1024;
            int bytesRead = 0;
            int offset = 0;

            while (bytesRead < n) {
                int read = inputStream.read(buffer, offset, n - bytesRead);
                if (read == -1) {
                    break;
                }
                bytesRead += read;
                offset += read;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error creating stream", e);
        }
        String result;
        try {
            result = new String(buffer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error converting body to string", e);
        }
        return result;
    }
}