package de.hellfish.ngrok.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class HttpResponseSerializer {
    private static final ObjectMapper objectMapper;

    static {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        objectMapper = new ObjectMapper(jsonFactory);
    }

    public static void writeToOutputStream(SerializableHttpResponse response, OutputStream outputStream)
            throws IOException {
        try {
            objectMapper.writeValue(outputStream, response);
        } catch (IOException e) {
            throw new IOException("Can't write http-response to socket: Server unavailable", e);
        }
    }

    public static SerializableHttpResponse readFromInputStream(InputStream inputStream) throws IOException {
        try {
            return objectMapper.readValue(inputStream, SerializableHttpResponse.class);
        } catch (IOException e) {
            throw new IOException("Can't read http-response socket: Client unavailable", e);
        }
    }
}