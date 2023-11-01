package de.hellfish.ngrok.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpRequestSerializerTest {
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testWriteToOutputStream() throws IOException {
        // given
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        HttpRequest request = new HttpRequest(headers, "GET", "/example", "Hello, World!".getBytes());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HttpRequestSerializer.writeToOutputStream(request, outputStream);
        HttpRequest deserializedRequest = objectMapper.readValue(outputStream.toByteArray(), HttpRequest.class);

        // then
        assertEquals(request, deserializedRequest);
    }

    @Test
    void testReadFromInputStream() throws IOException {
        // given
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        HttpRequest originalRequest = new HttpRequest(headers, "POST", "/sample", "Test data".getBytes());
        byte[] serializedRequest = objectMapper.writeValueAsBytes(originalRequest);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(serializedRequest);
        HttpRequest deserializedRequest = HttpRequestSerializer.readFromInputStream(inputStream);

        // then
        assertEquals(originalRequest, deserializedRequest);
    }
}