package de.hellfish.ngrok.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class HttpRequestSerializer {

    public static void writeToOutputStream(MyHttpRequest request, OutputStream outputStream) throws RuntimeException {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
        try {
            objectMapper.writeValue(outputStream, request);
        } catch (IOException e) {
            throw new RuntimeException("Can't write http-request to socket: Client is not available", e);
        }
    }

    public static MyHttpRequest readFromInputStream(InputStream inputStream) {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
        try {
            MyHttpRequest request = objectMapper.readValue(inputStream, MyHttpRequest.class);
            return request;
        } catch (IOException e) {
            throw new RuntimeException("Can't read http-request socket: Server is not available", e);
        }
    }

}