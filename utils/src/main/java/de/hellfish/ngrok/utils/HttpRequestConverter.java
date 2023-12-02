package de.hellfish.ngrok.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

public class HttpRequestConverter {
    private static final List<String> REQUEST_RESTRICTED_HEADERS = List.of(
            "content-length", "host", "connection", "upgrade");

    public static HttpRequest convertServletToNgrok(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, String> headers = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }

        byte[] body;
        try {
            InputStream inputStream = request.getInputStream();
            body = IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new HttpRequest(headers, request.getMethod(), request.getServerName(),
                request.getServerPort(), request.getRequestURI(), body);
    }

    public static java.net.http.HttpRequest convertNgrokToJava(HttpRequest inputRequest, String serviceProtocol,
                                                               int servicePort) {
        java.net.http.HttpRequest.Builder requestBuilder = java.net.http.HttpRequest.newBuilder()
                .uri(URI.create(serviceProtocol + "://" + "localhost" + ":" + servicePort + inputRequest.getUri()))
                .method(inputRequest.getMethod(), java.net.http.HttpRequest.BodyPublishers.ofByteArray(inputRequest.getBody()));
        for (Map.Entry<String, String> header : inputRequest.getHeaders().entrySet()) {
            if (!REQUEST_RESTRICTED_HEADERS.contains(header.getKey().toLowerCase())) {
                requestBuilder.header(header.getKey(), header.getValue());
            }
        }
        return requestBuilder.build();
    }
}