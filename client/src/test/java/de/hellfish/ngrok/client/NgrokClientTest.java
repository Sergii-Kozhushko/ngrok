package de.hellfish.ngrok.client;

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
public class NgrokClientTest {

    private ServerSocket testServerSocket;
    private NgrokClient client;
    private final int testPort = 8082;
    private String testLink = "http://w56h.myngrok.com";

    @BeforeEach
    public void setup() throws IOException, InterruptedException {
        // Test server
        testServerSocket = new ServerSocket(testPort);
        new Thread(() -> {
            try {
                try (Socket socket = testServerSocket.accept();
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                    out.println("LINK " + testLink);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        Thread.sleep(1000);

        client = new NgrokClient(testPort, "http");
    }

    @Test
    public void testClientServerInteraction() throws InterruptedException {
        Thread clientThread = new Thread(client);
        clientThread.start();
        Thread.sleep(1000);
        assertEquals(testLink, client.getLinkFromServer());
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (testServerSocket != null && !testServerSocket.isClosed()) {
            testServerSocket.close();
        }
    }
}