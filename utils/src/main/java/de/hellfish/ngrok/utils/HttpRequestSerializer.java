package de.hellfish.ngrok.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class HttpRequestSerializer {

    private static final ObjectMapper objectMapper;

    static {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        objectMapper = new ObjectMapper(jsonFactory);
    }

    public static void writeToOutputStream(HttpRequest request, OutputStream outputStream) throws RuntimeException {
        try {
            objectMapper.writeValue(outputStream, request);
        } catch (IOException e) {
            throw new RuntimeException("Can't write http-request to socket: Client is not available", e);
        }
    }

    public static HttpRequest readFromInputStream(InputStream inputStream) {
        try {
            HttpRequest request = objectMapper.readValue(inputStream, HttpRequest.class);
            return request;
        } catch (IOException e) {
            throw new RuntimeException("Can't read http-request socket: Server is not available", e);
        }
    }
}