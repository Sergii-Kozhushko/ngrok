package de.hellfish.ngrok.server;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
@TestPropertySource(properties = {"client.port=8080"})
public class ServerTest {

    @Autowired
    private Server server;

    private final ExecutorService testExecutor = Executors.newSingleThreadExecutor();

    @BeforeEach
    void setUp() {
        testExecutor.execute(server);
    }

    @Test
    void testServerAcceptsConnections() {
        try (Socket client = new Socket("localhost", 8080)) {
            assertTrue(client.isConnected());
        } catch (IOException e) {
            log.error("Error creating test client socket", e);
        }
    }

    @AfterEach
    void tearDown() {
        server.stopServer();
        testExecutor.shutdownNow();
    }
}