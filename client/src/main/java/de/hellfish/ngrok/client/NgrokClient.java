package de.hellfish.ngrok.client;

import de.hellfish.ngrok.utils.HttpRequestSerializer;
import de.hellfish.ngrok.utils.HttpResponseSerializer;
import de.hellfish.ngrok.utils.SerializableHttpResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Client for interacting with an ngrok server.
 * <p>
 * This class implements the client side of establishing a connection with an ngrok server.
 * The client establishes a socket connection with the server, sends
 * requests, and handles responses.
 * <p>
 * Usage:
 * Run in command line: java NgrokClient http 8080
 * <p>
 * On initialization, the client attempts to connect to the ngrok server at specified protocol
 * and port.
 */
@Slf4j
@Getter
@Setter
public class NgrokClient implements Runnable {

    private final int servicePort; // 8082
    private final String serviceProtocol;
    private String serverHost = "localhost";
    private int serverPort = 8082;
    private Socket serverSocket;
    private PrintWriter serverOut;
    private BufferedReader serverIn;
    private String linkFromServer;
    private boolean running;
    private Socket serviceSocket;
    private static final List<String> REQUEST_RESTRICTED_HEADERS = List.of(
            "content-length", "host", "connection", "upgrade");
    private de.hellfish.ngrok.utils.HttpRequest userRequest;

    public NgrokClient(String serviceProtocol, int servicePort) {
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
        new Thread(new NgrokClient(serviceProtocol, servicePort)).start();
    }

    @Override
    public void run() {
        if (!start()) {
            return;
        }
        running = initConnectionWithServer();
        while (running) {
            processRequest();
        }
        stop();
    }

    private void processRequest() {
        try {
            userRequest = HttpRequestSerializer.readFromInputStream(serverSocket.getInputStream());
            log.info("Client received new user request: {}", userRequest.toString());
        } catch (IOException e) {
            log.error("Error reading user request from server", e);
            running = false;
        }
        if (userRequest == null) {
            return;
        }

        HttpResponse<String> serviceResponse = null;
        try {
            serviceSocket = new Socket("localhost", servicePort);
            HttpClient httpClient = HttpClient.newHttpClient();
            serviceResponse = httpClient.send(convertHttpRequest(userRequest), HttpResponse.BodyHandlers.ofString());
            log.info("Client got response from service: {} with body {}", serviceResponse, serviceResponse.body());
        } catch (IOException | InterruptedException e) {
            log.error("Error sending http-request to service", e);
            sendResponse503ToServer();
        }

        if (serviceResponse == null) {
            return;
        }
        try {
            HttpResponseSerializer.writeToOutputStream(new SerializableHttpResponse(serviceResponse),
                    serverSocket.getOutputStream());
        } catch (IOException e) {
            log.error("Error sending response to server", e);
        }
    }

    private void sendResponse503ToServer() {
        try {
            HttpResponseSerializer.writeToOutputStream(
                    new SerializableHttpResponse(503,
                            "Ngrok Client response: Local Service at port " + servicePort + " unavailable"),
                    serverSocket.getOutputStream());
        } catch (IOException e) {
            log.error("Error sending 503 Response to server", e);
        }
    }

    private boolean start() {
        try {
            serverSocket = new Socket(serverHost, serverPort);
            serverOut = new PrintWriter(serverSocket.getOutputStream());
            serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        } catch (UnknownHostException e) {
            log.error("Error connecting to ngrok-server {}:{}. Host not found", serverHost, serverPort, e);
            return false;
        } catch (IOException e) {
            log.error("Error connecting to ngrok-server {}:{}. I/O Error", serverHost, serverHost, e);
            return false;
        }
        return true;
    }

    private void stop() {
        try {
            serverIn.close();
            serverOut.close();
            serverSocket.close();
            serviceSocket.close();
        } catch (IOException e) {
            log.error("Error closing socket", e);
        }
    }

    private HttpRequest convertHttpRequest(de.hellfish.ngrok.utils.HttpRequest inputRequest) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(serviceProtocol + "://" + "localhost" + ":" + servicePort + inputRequest.getUri()))
                .method(inputRequest.getMethod(), HttpRequest.BodyPublishers.ofByteArray(inputRequest.getBody()));
        for (Map.Entry<String, String> header : inputRequest.getHeaders().entrySet()) {
            if (!REQUEST_RESTRICTED_HEADERS.contains(header.getKey().toLowerCase())) {
                requestBuilder.header(header.getKey(), header.getValue());
            }
        }
        return requestBuilder.build();
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