package de.hellfish.ngrok.utils;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpResponseSerializerTest {

    @Test
    void shouldCorrectlySerializeAndDeserializeHttpResponse() throws IOException {
        // given
        SerializableHttpResponse initialResponse = new SerializableHttpResponse(200, "Body");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // when
        HttpResponseSerializer.writeToOutputStream(initialResponse, outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        SerializableHttpResponse deserializedResponse = HttpResponseSerializer.readFromInputStream(inputStream);

        // then
        assertEquals(initialResponse, deserializedResponse);
    }
}