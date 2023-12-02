package de.hellfish.ngrok.client;

import de.hellfish.ngrok.utils.HttpRequest;
import de.hellfish.ngrok.utils.HttpRequestSerializer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test to verify the interaction between the Ngrok client and a test server.
 * <p>
 * Test launches a simplified version of a server that accepts connections on a specific
 * port, sends back a fixed response, works 2 seconds and finishes.
 * <p>
 * Simultaneously the test starts the Ngrok client, which connects to
 * this server and awaits a link message.
 * After server shutdown, Ngrok client catches Socket exception and interrupts its thread
 */
@Slf4j
public class NgrokClientTest {

    private NgrokClient client;
    private final int serverPort = 8085;
    private final String testLink = "http://w56h.myngrok.com";
    private final int servicePort = 2224;
    private final String testHeaderKey = "content-type";
    private final String testHeaderValue = "text/plain";
    private final String testMethod = "POST";
    private final String testBody = "Test body";

    @BeforeEach
    public void setup() throws IOException, InterruptedException {
        // Test service
        new Thread(() -> {
            try (ServerSocket serviceServerSocket = new ServerSocket(servicePort);
                 Socket serviceSocket = serviceServerSocket.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(serviceSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(serviceSocket.getOutputStream(), true)
            ) {
                in.readLine();
                out.println("HTTP/1.1 200 OK\r\n"
                        + "Content-Length: 2\r\n"
                        + "\r\n"
                        + "OK");
            } catch (IOException e) {
                log.error("Error connecting to client", e);
            }
        }).start();

        // Test server
        new Thread(() -> {

            try (ServerSocket testServerSocket = new ServerSocket(serverPort);
                 Socket socket = testServerSocket.accept();
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            ) {
                out.println("LINK " + testLink);

                HttpRequest httpRequest = new HttpRequest(Map.of(testHeaderKey, testHeaderValue), testMethod,
                        "localhost", 51702, "/api", testBody.getBytes());
                HttpRequestSerializer.writeToOutputStream(httpRequest, socket.getOutputStream());
            } catch (IOException e) {
                log.error("Error connecting to ngrok-client from test server", e);
            }
        }).start();

        client = new NgrokClient("http", servicePort);
        client.setServerHost("localhost");
        client.setServerPort(serverPort);
    }

    @Test
    public void testClientServerInteraction() throws InterruptedException {
        // Given
        Thread clientThread = new Thread(client);

        //When
        clientThread.start();
        Thread.sleep(1000);

        // Then
        assertEquals(testLink, client.getLinkFromServer());
        assertEquals(client.getUserRequest().getMethod(), testMethod);
        assertEquals(client.getUserRequest().getHeaders().get(testHeaderKey), testHeaderValue);
        assertEquals(testBody, new String(client.getUserRequest().getBody(), StandardCharsets.UTF_8));
    }
}