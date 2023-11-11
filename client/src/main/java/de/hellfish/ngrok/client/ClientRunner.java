package de.hellfish.ngrok.client;

import de.hellfish.ngrok.utils.HttpRequest;
import de.hellfish.ngrok.utils.HttpRequestSerializer;
import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Slf4j
public class ClientRunner {
    private static int servicePort; // 8082
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8082;
    private static Socket serverSocket;

    public static void main(String[] args) {

        if ((servicePort = fetchServicePort(args)) == 0) {
            log.error("Provide service port in command line argument -port");
            return;
        }
        boolean running = initConnectionWithServer();
        HttpRequest userRequest;
        while (running) {
            try {
                userRequest = HttpRequestSerializer.readFromInputStream(serverSocket.getInputStream());
                log.info(String.format("Client received new user request: %s", userRequest.toString()));
            } catch (IOException e) {
                log.error("Error connecting to server", e);
                running = false;
            }
        }
    }

    private static int fetchServicePort(String[] args) {
        int servicePort = 0;
            for (String arg : args) {
                if (arg.contains("-port=")) {
                    try {
                        servicePort = Integer.parseInt(arg.substring(6));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        return servicePort;
    }

    public static boolean initConnectionWithServer() {
        try {
            serverSocket = new Socket(SERVER_HOST, SERVER_PORT);
            PrintWriter serverOut = new PrintWriter(serverSocket.getOutputStream());
            BufferedReader serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

            serverOut.print("HTTP " + servicePort + "\n");
            serverOut.flush();
            String serverReply = serverIn.readLine();
            String serverReplyCode = serverReply.split(" ")[0];

            if (serverReplyCode.equals("LINK")) {
                String link = serverReply.split(" ")[1];
                log.info(String.format("Link for user requests: http://localhost:%s -> %s", servicePort, link));
                return true;
            }
            if (serverReplyCode.equals("ERROR")) {
                log.error(serverReply.substring("ERROR ".length()));
                return false;
            }
            log.error("Unrecognized reply from server");
        } catch (IOException e) {
            log.error("Server is not available", e);
            return false;
        }
        return true;
    }
}