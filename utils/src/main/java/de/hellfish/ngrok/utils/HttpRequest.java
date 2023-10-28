package de.hellfish.ngrok.utils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@NoArgsConstructor
@Setter
@Getter
public class HttpRequest {

    private Map<String, String> headers = new HashMap<>();
    private String method;
    private String url;

    private byte[] body;

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

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




