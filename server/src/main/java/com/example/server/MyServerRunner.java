package com.example.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

@Component
public class MyServerRunner implements CommandLineRunner {
    private static final int serverPort = 8081;

    private static final int clientPort = 8082;
    public static final String clientHost = "localhost";

    @Autowired
    public static final Logger logger = LoggerFactory.getLogger(MyServerRunner.class);

    @Override
    public void run(String... args) throws Exception {
        if(args.length > 0) {
            logger.info("first command-line parameter: '{}'", args[0]);
        }
        ServerSocket serverSocket = null;
        Socket userSocket = null;
        try {
            serverSocket = new ServerSocket(serverPort);
            System.out.println("Server started at port: " + serverPort);
            while (true) {
                userSocket = serverSocket.accept();
                System.out.print("New user connected. ");
                System.out.println("Host: " + userSocket.getLocalAddress() + ":" + userSocket.getPort());
                // Создаем поток для обработки запроса
                new ClientHandler(userSocket).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            try {
                Objects.requireNonNull(serverSocket).close();
                System.out.println("Server closed");
                Objects.requireNonNull(userSocket).close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    static class ClientHandler extends Thread {
        private Socket userSocket;

        // Конструктор с параметром сокета
        public ClientHandler(Socket userSocket) {
            this.userSocket = userSocket;
        }

        @Override
        public void run() {
            try {
                // Получаем входной поток от пользователя из браузера для чтения данных
                BufferedReader input = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));

                // читаем запрос от браузера и сразу передаем запрос клиенту
                Socket clientSocket = new Socket(clientHost, clientPort);

//                OutputStream clientOutStream = clientSocket.getOutputStream();
//
//                String line;
//                while ((line = input.readLine()) != null) {
//                    System.out.println(line);
//                    clientOutStream.write(line.getBytes());
//                }
//                clientOutStream.write("\r\n".getBytes());
//                clientOutStream.flush();
                PrintWriter clientOutStream = new PrintWriter(clientSocket.getOutputStream(), true);
                String line;
                while ((line = input.readLine()) != null) {
                    System.out.println(line);
                    clientOutStream.write(line);
                }
                clientOutStream.flush();


                // читаем ответ от клиента
                InputStream clientInputStream = clientSocket.getInputStream();
                BufferedReader clientResponseReader = new BufferedReader(new InputStreamReader(clientInputStream));

                // отправляем ответ обратно браузеру
                OutputStream output = clientSocket.getOutputStream();
                String responseLine;
                while ((responseLine = clientResponseReader.readLine()) != null) {
                    output.write(responseLine.getBytes());
                }
                output.flush();

                // Закрываем потоки и соединение
                output.close();
                input.close();
                clientInputStream.close();
                clientSocket.close();
                clientResponseReader.close();
                System.out.println("Client thread is closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
