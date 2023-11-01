package de.hellfish.ngrok.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Map;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class MyHttpRequest {

    private Map<String, String> headers;
    private String method;
    private String url;
    private String uri;
    private byte[] body;

    @Override
    public String toString() {
        return "MyHttpRequest{" +
                "headers=" + headers +
                ", method='" + method + '\'' +
                ", url='" + url + '\'' +
                ", body=" + Arrays.toString(body) +
                '}';
    }
}




