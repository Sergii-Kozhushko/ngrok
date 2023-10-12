package de.hellfish.ngrok;

import java.io.*;
import java.net.Socket;

public class SimpleHttpTunnelClient {
    public static void main(String[] args) throws IOException {
        int tunnelServerPort = 8080; // Порт сервера туннелирования
        String tunnelServerHost = "localhost"; // Адрес сервера туннелирования

        Socket tunnelSocket = new Socket(tunnelServerHost, tunnelServerPort);
        System.out.println("Connected to tunnel server");

        // Создание HTTP-запроса для отправки на сервер
        String httpRequest = "GET / HTTP/1.1\r\nHost: google.com\r\n\r\n";

        // Отправка HTTP-запроса через туннель
        OutputStream tunnelOutput = tunnelSocket.getOutputStream();
        tunnelOutput.write(httpRequest.getBytes());

        // Чтение HTTP-ответа от сервера через туннель
        InputStream tunnelInput = tunnelSocket.getInputStream();
        BufferedReader tunnelReader = new BufferedReader(new InputStreamReader(tunnelInput));
        String responseLine;

        while ((responseLine = tunnelReader.readLine()) != null) {
            System.out.println(responseLine);
        }

        tunnelSocket.close();
    }
}
