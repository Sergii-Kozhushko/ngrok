package de.hellfish.ngrok.utils;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpRequestSerializerTest {

    @Test
    void testWriteToOutputStreamReadFromInputStream() throws IOException {
        // given
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        MyHttpRequest request = new MyHttpRequest(headers, "GET",
                "sub1.localhost", 9000, "/example", "Hello, World!".getBytes());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HttpRequestSerializer.writeToOutputStream(request, outputStream);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        MyHttpRequest deserializedRequest = HttpRequestSerializer.readFromInputStream(inputStream);

        // then
        assertEquals(request, deserializedRequest);
    }

}