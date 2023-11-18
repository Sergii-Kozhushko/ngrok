package de.hellfish.ngrok.client;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test to verify the interaction between the Ngrok client and a test server.
 * <p>
 * Test launches a simplified version of a server that accepts connections on a specific
 * port, sends back a fixed response, works 2 seconds and finishes.
 * <p>
 * Simultaneously the test starts the Ngrok client, which connects to
 * this server and awaits a link message.
 * After server shutdown, Ngrok client catches Socket exception and interrupt its thread
 */
@Slf4j
public class NgrokClientTest {

    private ServerSocket testServerSocket;
    private NgrokClient client;
    private final int testPort = 8082;
    private final String testLink = "http://w56h.myngrok.com";

    @BeforeEach
    public void setup() throws IOException, InterruptedException {
        // Test server
        testServerSocket = new ServerSocket(testPort);
        new Thread(() -> {
            try {
                try (Socket socket = testServerSocket.accept();
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                    out.println("LINK " + testLink);
                    Thread.sleep(2000);
                }
            } catch (IOException e) {
                log.error("Error connecting to ngrok-client from test server", e);
            } catch (InterruptedException e) {
                log.error("Test server thread was interrupted", e);

            }
        }).start();
        Thread.sleep(1000);

        client = new NgrokClient(testPort, "http");
    }

    @Test
    public void testClientServerInteraction(){
        Thread clientThread = new Thread(client);
        clientThread.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("Client thread was interrupted", e);
        }
        assertEquals(testLink, client.getLinkFromServer());
    }

    @AfterEach
    public void closeServerSocket() {
        try {
            if (testServerSocket != null && !testServerSocket.isClosed()) {
                testServerSocket.close();
            }
        } catch (IOException e) {
            log.error("Error closing test server socket", e);
        }
    }
}