package de.hellfish.ngrok.utils;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class HttpRequestConverterTest {

    @Mock
    private HttpServletRequest mockRequest;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void closeService() throws Exception {
        closeable.close();
    }

    @Test
    void testConvert() throws IOException {
        // given
        Map<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put("Header1", "Value1");
        expectedHeaders.put("Header2", "Value2");
        byte[] expectedBody = "body".getBytes();

        // when
        Set<String> headerNames = expectedHeaders.keySet();
        Enumeration<String> headerNamesEnumeration = new Vector<>(headerNames).elements();
        Mockito.when(mockRequest.getHeaderNames()).thenReturn(headerNamesEnumeration);
        for (String headerName : headerNames) {
            Mockito.when(mockRequest.getHeader(headerName)).thenReturn(expectedHeaders.get(headerName));
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(expectedBody);

        Mockito.when(mockRequest.getInputStream()).thenAnswer(invocation -> new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {

            }

            @Override
            public int read() throws IOException {
                return inputStream.read();
            }
        });
        when(mockRequest.getMethod()).thenReturn("GET");
        when(mockRequest.getRequestURI()).thenReturn("/test-uri");
        MyHttpRequest myHttpRequest = HttpRequestConverter.convertServletToMy(mockRequest);

        // then
        assertEquals(expectedHeaders, myHttpRequest.getHeaders());
        assertEquals("GET", myHttpRequest.getMethod());
        assertEquals("/test-uri", myHttpRequest.getUri());
        assertArrayEquals(expectedBody, myHttpRequest.getBody());
    }
}