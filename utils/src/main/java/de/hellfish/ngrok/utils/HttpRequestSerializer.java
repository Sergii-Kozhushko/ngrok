package de.hellfish.ngrok.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


@Slf4j
public class HttpRequestSerializer {

    public static void writeToOutputStream(HttpRequest request, OutputStream outputStream) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(outputStream, request);
        } catch (IOException e) {
            log.error("Error writing HttpRequest to output stream", e);
        }
    }

    public static HttpRequest readFromInputStream(InputStream inputStream) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            HttpRequest request = objectMapper.readValue(inputStream, HttpRequest.class);
            return request;
        } catch (IOException e) {
            log.error("Error reading HttpRequest from stream", e);
        }
        return null;
    }
}
