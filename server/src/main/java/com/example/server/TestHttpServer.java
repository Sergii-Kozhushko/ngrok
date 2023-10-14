package com.example.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TestHttpServer {
    public static void main(String[] args) throws IOException {
        int serverPort = 8080; // Порт на котором работает сервер

        ServerSocket serverSocket = new ServerSocket(serverPort);
        System.out.println("HTTP Tunnel Server listening on port " + serverPort);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new HttpTunnelThread(clientSocket).start();
        }
    }
}

class HttpTunnelThread extends Thread {
    private Socket clientSocket;

    public HttpTunnelThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            // Чтение HTTP-запроса от клиента
            BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String clientRequest = clientReader.readLine();

            // Парсинг метода, пути и версии HTTP
            String[] requestParts = clientRequest.split(" ");
            String method = requestParts[0];
            String path = requestParts[1];

            // Определите ваш локальный сервер (например, порт 80)
            int localServerPort = 80;
            String localServerHost = "localhost";

            // Создание соединения с локальным сервером
            Socket localServerSocket = new Socket(localServerHost, localServerPort);

            // Пересылка HTTP-запроса на локальный сервер
            OutputStream localServerOutput = localServerSocket.getOutputStream();
            localServerOutput.write(clientRequest.getBytes());

            // Чтение HTTP-ответа от локального сервера
            InputStream localServerInput = localServerSocket.getInputStream();
            BufferedReader localServerReader = new BufferedReader(new InputStreamReader(localServerInput));
            String responseLine;

            // Отправка HTTP-ответа обратно клиенту
            OutputStream clientOutput = clientSocket.getOutputStream();
            while ((responseLine = localServerReader.readLine()) != null) {
                clientOutput.write(responseLine.getBytes());
            }

            // Закрытие соединений
            clientSocket.close();
            localServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
