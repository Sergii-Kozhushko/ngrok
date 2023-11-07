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

@Slf4j
@Component
public class ClientRunner implements CommandLineRunner {
    private static int servicePort;
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8082;
    private static boolean running = true;
    private Socket serverSocket;
    private PrintWriter serverOut;
    private BufferedReader serverIn;


    @Override
    public void run(String... args) {
        if ((servicePort = fetchServicePort(args)) == 0) {
            log.error(Messages.PROVIDE_SERVICE_PORT);
            return;
        }
        running = initConnectionWithServer();
        HttpRequest userRequest;
        while (running) {
            try {
                userRequest = HttpRequestSerializer.readFromInputStream(serverSocket.getInputStream());
                log.info(String.format(Messages.NEW_USER_REQUEST, userRequest.toString()));
            } catch (IOException e) {
                log.error(Messages.ERROR_SERVER_IS_DOWN, e);
                running = false;
            }
        }
    }

    private int fetchServicePort(String[] args) {
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

    public boolean initConnectionWithServer() {
        try {
            serverSocket = new Socket(SERVER_HOST, SERVER_PORT);
            serverOut = new PrintWriter(serverSocket.getOutputStream());
            serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

            serverOut.print("HTTP " + servicePort + "\n");
            serverOut.flush();
            String serverReply = serverIn.readLine();
            String serverReplyCode = serverReply.split(" ")[0];

            if (serverReplyCode.equals("LINK")) {
                String link = serverReply.split(" ")[1];
                log.info(String.format(Messages.LINK_FOR_USER,  servicePort, link));
                return true;
            }
            if (serverReplyCode.equals("ERROR")) {
                log.error(serverReply.substring("ERROR ".length()));
                return false;
            }
            log.error(Messages.ERROR_REPLY_SERVER);
        } catch (IOException e) {
            log.error(Messages.ERROR_CONNECTION_SERVER, e);
            return false;
        }
        return true;
    }
}