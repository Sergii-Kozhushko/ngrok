package de.hellfish.ngrok.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

@Component
@Slf4j
public class ClientRunner implements CommandLineRunner {
    private final String serverUri = "http://server.localhost:8080";
    private final String clientRequest = "?q=HTTP+2222";// test initial request from client to server

    private static final int clientPort = 8082;
    private static int servicePort = 2222;
    private static final String serverHost = "localhost";
    private static final int serverPort = 8082;
    private static boolean running = true;


    @Override
    public void run(String... args) {
        try  {
            Socket socket = new Socket(serverHost, serverPort);
            PrintWriter serverOut = new PrintWriter(socket.getOutputStream());
            BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            serverOut.print("HTTP " + servicePort +"\n");
            serverOut.flush();
            String line;
            StringBuilder request = new StringBuilder();
            while (!serverIn.ready()) ;
            String serverReply = serverIn.readLine();

            // сервер прислал ошибку
            if (serverReply.split(" ")[0].equals("ERROR")) {

                log.error(serverReply.substring("ERROR ".length()));
                running = false;
            } else {
                String link = serverReply.split(" ")[1];
                log.info("Link for user requests: " + servicePort + " -> " + link);
            }

            while (running) {
                while (!serverIn.ready()) ;
                while ((line = serverIn.readLine()) != null && !line.isEmpty()) {
                    request.append(line).append("\r\n");
                }
                System.out.println("Client got message from server: \n" + request);
                // get user number
                String userNumber = request.toString().split("\r\n")[0].split(" ")[1];
                String message = request.substring(("USER " + userNumber+"\r\n").length());

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

    private static StringBuilder exchangePacketsWithService(String request) {
        StringBuilder response = new StringBuilder();
        try
        {
            Socket socket = new Socket("localhost", servicePort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //send request to service
            out.print(request + "\n");
            out.flush();

            // get response from service
            String line;
            while ((line = in.readLine()) != null ) {
                response.append(line).append("\r\n");
            }
        } catch (IOException e) {
            log.error("Error exchanging data with service", e);
        }
        return response;
    }
}