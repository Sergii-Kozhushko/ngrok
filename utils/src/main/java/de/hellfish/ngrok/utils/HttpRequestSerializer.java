package de.hellfish.ngrok.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HttpRequestSerializer {

    public static void writeToOutputStream(HttpRequest request, OutputStream outputStream) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(outputStream, request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HttpRequest readFromInputStream(InputStream inputStream) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            HttpRequest request = objectMapper.readValue(inputStream, HttpRequest.class);
            return request;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
