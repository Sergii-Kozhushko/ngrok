package de.hellfish.ngrok.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Arrays;
import java.util.Map;


@Getter
@RequiredArgsConstructor
public final class HttpRequest {

    private final Map<String, String> headers;
    private final String method;
    private final String url;
    private final byte[] body;

    @Override
    public String toString() {
        return "HttpRequest{" +
                "headers=" + headers +
                ", method='" + method + '\'' +
                ", url='" + url + '\'' +
                ", body=" + Arrays.toString(body) +
                '}';
    }
}




