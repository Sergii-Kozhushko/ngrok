package de.hellfish.ngrok.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

public class HttpRequestConverter {

    public static HttpRequest convert(HttpServletRequest request) {
        HttpRequest result = new HttpRequest();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            result.addHeader(headerName, headerValue);
        }
        result.setMethod(request.getMethod());
        result.setUrl(request.getRequestURI());

        try {
            InputStream inputStream = request.getInputStream();
            result.setBody(IOUtils.toByteArray(inputStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
