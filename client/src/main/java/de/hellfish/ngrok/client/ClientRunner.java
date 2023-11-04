package de.hellfish.ngrok.client;

import de.hellfish.ngrok.utils.HttpRequestSerializer;
import de.hellfish.ngrok.utils.MyHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


@Slf4j
@Component
public class ClientRunner implements CommandLineRunner {
    private static int servicePort = 2222;
    private static final String serverHost = "localhost";
    private static final int serverPort = 8082;
    private static boolean running = true;
    private static Socket serverSocket;
    private static PrintWriter serverOut;
    private static BufferedReader serverIn;


    @Override
    public void run(String... args) {
        running = initConnectionWithServer();
        MyHttpRequest userRequest;
        while (running) {
            try {
                userRequest = HttpRequestSerializer.readFromInputStream(serverSocket.getInputStream());
                log.info("Client received new user request: " + userRequest.toString());
            } catch (IOException e) {
                log.error("Error reading data from server", e);
                running = false;
            }
        }
    }

    private boolean initConnectionWithServer() {
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
                return false;
            } else if (serverReplyCode.equals("LINK")) {
                String link = serverReply.split(" ")[1];
                log.info("Link for user requests: http://localhost:" + servicePort + " -> " + link);
            } else {
                log.error("Unknown reply from server");
                return false;
            }
        } catch (IOException e) {
            log.error("Error establishing connection with server", e);
            return false;
        }
        return true;
    }

}