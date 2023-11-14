package de.hellfish.ngrok.client;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Slf4j
public class TestService {

    private static final int testServicePort = 2222;
    private static final int clientPort = 8082;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(testServicePort)) {
            System.out.println("Test Service started! Listening on: " + testServicePort);
            while (true) {
                Socket socket = serverSocket.accept();
                // new request from client
                System.out.print("Someone connected. ");
                System.out.println(" Host: " + socket.getLocalAddress() + ":" + socket.getPort());
                try (
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(socket.getInputStream(),
                                        StandardCharsets.UTF_8));
                        PrintWriter writer = new PrintWriter(socket.getOutputStream())
                ) {
                    StringBuilder request = new StringBuilder();
                    while (reader.ready()) {
                        request.append(reader.readLine());
                        request.append("\r\n");

                    }
                    log.info("TestService got request:\n" + request);

                    String reply200 = "HTTP/1.1 200 OK\r\n"
                            + "Date: Fri, 14 Oct 2023 10:00:00 GMT\r\n"
                            + "Server: Apache/2.4.41 (Unix)\r\n"
                            + "Content-Length: 4\r\n"
                            + "Content-Type: text/plain; charset=utf-8"
                            + "\r\n\r\n"
                            + "test";

                    writer.write(reply200);
                    writer.flush();
                }
            }
        } catch (IOException e) {
            log.error("Test service error", e);
        }
    }
}