package de.hellfish.ngrok.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;


@Slf4j
public class HttpRequestSerializer {

    public static void writeToOutputStream(MyHttpRequest request, Socket socket) {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
        try {
            objectMapper.writeValue(socket.getOutputStream(), request);
        } catch (IOException e) {
            log.error("Error writing MyHttpRequest to output stream", e);
        }
    }

    public static MyHttpRequest readFromInputStream(Socket socket) {


        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        ObjectMapper objectMapper = new ObjectMapper(jsonFactory);


        try {
            MyHttpRequest request = objectMapper.readValue(socket.getInputStream(), MyHttpRequest.class);
            return request;
        } catch (IOException e) {
            log.error("Error reading MyHttpRequest from input stream", e);
        }
        return null;
    }
}
