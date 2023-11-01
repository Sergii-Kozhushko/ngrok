package de.hellfish.ngrok.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestConverter {

    public static MyHttpRequest convertServletToMy(HttpServletRequest request) {
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
        String uri = request.getContextPath() + request.getServletPath();
        return new MyHttpRequest(headers, request.getMethod(),
                request.getRequestURI(), uri, body);
    }

    public static HttpRequest convertMyToHttp(MyHttpRequest myHttpRequest, String serviceAddress) {
        // Создаем URI из URL
        String userUrl = serviceAddress + myHttpRequest.getUri();
        URI uri = URI.create(userUrl);

        // Преобразуем заголовки из MyHttpRequest в формат, понятный для HttpRequest
        Map<String, String> headers = new HashMap<>();
        myHttpRequest.getHeaders().forEach((key, value) -> headers.put(key, value));

        // Преобразуем тело запроса в байтовый массив


        // Создаем HTTP-запрос
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(uri)
                .method(myHttpRequest.getMethod(), HttpRequest.BodyPublishers.ofString(
                        new String(myHttpRequest.getBody(), StandardCharsets.UTF_8)));
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equals("content-length") || entry.getKey().equals("host") ||
                    entry.getKey().equals("connection")) {
                continue;
            }
            requestBuilder = requestBuilder.header(entry.getKey(), entry.getValue());
        }

        return requestBuilder.build();

    }


}
