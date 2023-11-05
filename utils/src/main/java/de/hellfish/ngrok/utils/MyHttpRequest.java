package de.hellfish.ngrok.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Getter
@ToString
@EqualsAndHashCode
public final class MyHttpRequest {
    private final Map<String, String> headers;
    private final String method;
    private final String server; //sub1.localhost
    private final int port; // 9000
    private final String uri;
    private final byte[] body;

    @JsonCreator
    public MyHttpRequest(@JsonProperty("headers")Map<String, String> headers,
                         @JsonProperty("method") String method,
                         @JsonProperty("server") String server,
                         @JsonProperty("port") int port,
                         @JsonProperty("uri") String uri,
                         @JsonProperty("body") byte[] body) {
        this.headers = headers;
        this.method = method;
        this.server = server;
        this.port = port;
        this.uri = uri;
        this.body = body;
    }
}