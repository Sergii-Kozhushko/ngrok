package de.hellfish.ngrok.client;

import de.hellfish.ngrok.utils.HttpRequest;
import de.hellfish.ngrok.utils.HttpRequestSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

@Component
@Slf4j
public class ClientRunner implements CommandLineRunner {
    private static final int clientPort = 8082;
    private static int servicePort = 2222;
    private static final String serverHost = "localhost";
    private static final int serverPort = 8082;
    private static boolean running = true;
    private static Socket serverSocket;
    private static PrintWriter serverOut;
    private static BufferedReader serverIn;


    @Override
    public void run(String... args) {
        try {
            try {
                initConnection();
            } catch (RuntimeException e) {
                log.error("Error establishing connection with server", e);
            }

            while (running) {
                // ByteArrayInputStream inputStream = new ByteArrayInputStream(serverSocket.getInputStream().readAllBytes());
                HttpRequest userRequest = HttpRequestSerializer.readFromInputStream(serverSocket.getInputStream());
                log.info("Client received new user request: " + userRequest.toString());
                var response = sendRequestToService(userRequest);

                while (!serverIn.ready()) ;
                while ((line = serverIn.readLine()) != null && !line.isEmpty()) {
                    request.append(line).append("\r\n");
                }
                System.out.println("Client got message from server: \n" + request);
                // get user number
                String userNumber = request.toString().split("\r\n")[0].split(" ")[1];
                String message = request.substring(("USER " + userNumber + "\r\n").length());

                // send request to service and wait for reply
                StringBuilder responseFromService = exchangePacketsWithService(message);

                log.info("Client got response from service:\n" + responseFromService);

                // send request back to server
                String response = "USER " + userNumber + "\r\n" + responseFromService + "\n";
                serverOut.print(response);
                serverOut.flush();
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("Server is not reachable on " + serverHost + ":" + serverPort, e);
        }
    }

    private void initConnection() throws RuntimeException {
        try {
            serverSocket = new Socket(serverHost, serverPort);
            serverOut = new PrintWriter(serverSocket.getOutputStream());
            serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

            serverOut.print("HTTP " + servicePort + "\n");
            serverOut.flush();
            String serverReply = serverIn.readLine();
            String serverReplyCode = serverReply.split(" ")[0];

            if (serverReplyCode.equals("ERROR")) {
                log.error(serverReply.substring("ERROR ".length()));
                running = false;
            } else if (serverReplyCode.equals("LINK")) {
                String link = serverReply.split(" ")[1];
                log.info("Link for user requests: http://localhost:" + servicePort + " -> " + link);
            } else {
                log.error("Unknown reply from server");
                running = false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpResponse<String> sendRequestToService(HttpRequest userRequest) {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest serviceRequest = HttpRequestConverter.
                convertMyToHttp(userRequest, "http://localhost:" + servicePort);

        try {
            // Отправка запроса и получение ответа
            HttpResponse<String> response = client.send(serviceRequest, HttpResponse.BodyHandlers.ofString());

            // Обработка ответа
            int statusCode = response.statusCode();
            String responseBody = response.body();

            System.out.println("Status Code: " + statusCode);
            System.out.println("Response Body:");
            System.out.println(responseBody);
            return response;

        } catch (Exception e) {
            log.error("Service not available", e);
            return null;
        }
    }

    private static StringBuilder exchangePacketsWithService(String request) {
        StringBuilder response = new StringBuilder();
        try {
            Socket socket = new Socket("localhost", servicePort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //send request to service
            out.print(request + "\n");
            out.flush();

            // get response from service
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line).append("\r\n");
            }
        } catch (IOException e) {
            log.error("Error exchanging data with service", e);
        }
        return response;
    }
}
