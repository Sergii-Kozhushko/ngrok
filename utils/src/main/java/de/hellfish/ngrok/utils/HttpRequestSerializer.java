package de.hellfish.ngrok.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class HttpRequestSerializer {

    public static void writeToOutputStream(HttpRequest request, OutputStream outputStream) throws RuntimeException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(outputStream, request);
        } catch (IOException e) {
            throw new RuntimeException("Client is not available", e);
        }
    }

    public static HttpRequest readFromInputStream(InputStream inputStream) throws RuntimeException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            HttpRequest request = objectMapper.readValue(inputStream, HttpRequest.class);
            return request;
        } catch (IOException e) {
            throw new RuntimeException("Server is not available", e);
        }
    }
}