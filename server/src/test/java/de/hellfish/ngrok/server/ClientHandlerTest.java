package de.hellfish.ngrok.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class ClientHandlerTest {

    @Mock
    private Socket clientSocket;

    private Map<String, Socket> clientList;
    private ByteArrayOutputStream outputStream;

    @InjectMocks
    private ClientHandler clientHandler;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);
        clientList = new HashMap<>();
        outputStream = new ByteArrayOutputStream();
        ByteArrayInputStream inputStream = new ByteArrayInputStream("HTTP 8080\n".getBytes());
        when(clientSocket.getOutputStream()).thenReturn(new PrintStream(outputStream));
        when(clientSocket.getInputStream()).thenReturn(inputStream);
        clientHandler = new ClientHandler(clientSocket, clientList);
    }

    @Test
    public void testRunMethod() {
        clientHandler.run();
        assertFalse(clientList.isEmpty());
        String response = outputStream.toString();
        assertTrue(response.startsWith("LINK "), "Ответ сервера должен начинаться с 'LINK http://'");
    }
}