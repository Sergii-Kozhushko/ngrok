package de.hellfish.ngrok.client;

import de.hellfish.ngrok.utils.HttpRequest;
import de.hellfish.ngrok.utils.HttpRequestSerializer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

@Slf4j
@Setter
@Getter
public class NgrokClient implements Runnable {

    private int servicePort; // 8082
    private String serviceProtocol;
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8082;
    private Socket serverSocket;
    private PrintWriter serverOut;
    private BufferedReader serverIn;
    private String linkFromServer;
    private boolean running;

    public NgrokClient(int servicePort, String serviceProtocol) {
        this.servicePort = servicePort;
        this.serviceProtocol = serviceProtocol;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            log.error("Provide service protocol and port. Syntax: java NgrokClient <protocol> <port>");
            return;
        }
        String serviceProtocol = args[0];
        int servicePort = 0;
        try {
            servicePort = Integer.parseInt(args[1]);
        } catch (NumberFormatException ignored) {
            log.error("Wrong service port provided. Syntax: java NgrokClient <protocol> <port>");
            return;
        }
        if (!serviceProtocol.equalsIgnoreCase("http")) {
            log.error("Protocol '{}' not supported. Syntax: java NgrokClient <protocol> <port>", serviceProtocol);
            return;
        }
        new Thread(new NgrokClient(servicePort, serviceProtocol)).start();
    }

    @Override
    public void run() {
        if (!start()) {
            return;
        }
        running = initConnectionWithServer();
        while (running) {
                try {
                    HttpRequest userRequest = HttpRequestSerializer.readFromInputStream(serverSocket.getInputStream());
                    log.info("Client received new user request: {}", userRequest.toString());
                } catch (IOException e) {
                    log.error("Server is down");
                    running = false;
                }
        }
        stop();
    }

    private boolean start() {
        try {
            serverSocket = new Socket(SERVER_HOST, SERVER_PORT);
            serverOut = new PrintWriter(serverSocket.getOutputStream());
            serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        } catch (UnknownHostException e) {
            log.error("Error connecting to ngrok-server {}:{}. Host not found", SERVER_HOST, SERVER_PORT, e);
            return false;
        } catch (IOException e) {
            log.error("Error connecting to ngrok-server {}:{}. I/O Error", SERVER_HOST, SERVER_HOST, e);
            return false;
        }
        return true;
    }

    private void stop() {
        try {
            serverIn.close();
            serverOut.close();
            serverSocket.close();
        } catch (IOException e) {
            log.error("Could not close ngrok-server socket", e);
        }
    }

    private boolean initConnectionWithServer() {
        try {
            serverOut.print(serviceProtocol + " " + servicePort + "\n");
            serverOut.flush();
            String serverReply = serverIn.readLine();
            String serverReplyCode = serverReply.split(" ")[0];

            if (serverReplyCode.equals("LINK")) {
                linkFromServer = serverReply.split(" ")[1];
                log.info("Session status: online");
                log.info("Forwarding: {} -> {}://localhost:{}", linkFromServer, serviceProtocol, servicePort);
                return true;
            }
            if (serverReplyCode.equals("ERROR")) {
                log.info("Session status: offline");
                log.error(serverReply.substring("ERROR ".length()));
                return false;
            }
            log.error("Unrecognized reply from server");
            return false;
        } catch (IOException e) {
            log.error("Error connecting to server", e);
            return false;
        }
    }
}