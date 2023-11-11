package de.hellfish.ngrok.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;


@SpringBootTest
public class ClientHandlerTest {

    @Mock
    private Socket clientSocket;

    @Autowired
    private ClientConnectionService clientList;

    private ByteArrayOutputStream outputStream;

    @InjectMocks
    private ClientHandler clientHandler;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);

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