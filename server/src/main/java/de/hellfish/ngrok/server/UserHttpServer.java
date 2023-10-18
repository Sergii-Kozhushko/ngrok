package de.hellfish.ngrok.server;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Setter
public class UserHttpServer extends Thread {
    private int userPort;
    private Socket clientSocket; //соединение клиента, которое инициировало принимать запросы  юзеров на этот порт
    private boolean running = true;
    private ServerSocket userServerSocket;

    private Map<Integer, Socket> userConnections; // список юзеров, которые установили соединение

    public static final Logger logger = LoggerFactory.getLogger(UserHttpServer.class);

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
            logger.error(userPort + " is busy. Users Server can't run");
            e.printStackTrace();
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
                //  получили сообщение от юзера
                var userSocket = userServerSocket.accept();
                userConnections.put(userSocket.getPort(), userSocket);
                logger.info("New user connected from " + userSocket.getLocalAddress() + ":" + userSocket.getPort());


                executors.submit(() -> {
                    try {
                        // читаем запрос побайтово
                        // readUserRequestByte(Socket socket)
                        BufferedReader userInputStream = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
                        //  читаем содержимое сообщения юзера
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

                        // читаем тело
                        if (contentLength > 0) {
                            //readBody(contentLength, userSocket);

                        }



                        if (userRequest.length() == 0) {
                            this.interrupt();
                        }
                        logger.info("User handler got request from " +
                                userSocket.getLocalAddress() + ":" + userSocket.getPort() + "\n" + "*" + userRequest + "*");

                        // переслать полученный юзерский запрос клиенту
                        // добавить в начале USER [ПОРТ] чтобы различать какой пакет какому юзеру вернуть
                        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                        writer.write("USER " + userSocket.getPort() + "\r\n" + userRequest);
                        writer.write("\n");

                        writer.flush();

                        // прочитать ответ клиента и отправить юзеру
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
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readBody(int contentLength, Socket socket) {
        byte[] buffer = new byte[contentLength];
        try {
            InputStream inputStream = socket.getInputStream();

            int n = 1024; // Кількість байт, які ви хочете прочитати


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

// *** рабочий тест
//                        PrintWriter writer2 = new PrintWriter(userSocket.getOutputStream());
//                        String reply200 = "HTTP/1.1 200 OK\r\n" +
//                                "Date: Fri, 14 Oct 2023 10:00:00 GMT\r\n" +
//                                "Server: Apache/2.4.41 (Unix)\r\n" +
//                                "Content-Length: 4\r\n" +
//                                "Content-Type: text/plain; charset=utf-8" +
//                                "\r\n\r\n" +
//                                "test";
//                        writer2.write(reply200);
//                        writer2.flush();
//***
