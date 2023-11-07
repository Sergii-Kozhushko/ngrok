package de.hellfish.ngrok.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class HttpRequestConverter {
    public static Set<String> excludedHeaderNames;

    static {
        excludedHeaderNames = new HashSet<>();
        excludedHeaderNames.add("content-length");
        excludedHeaderNames.add("host");
        excludedHeaderNames.add("connection");
    }

    public static HttpRequest convert(HttpServletRequest request) {
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
}