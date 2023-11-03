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
public final class HttpRequest {
    private final Map<String, String> headers;
    private final String method;
    private final String url;
    private final byte[] body;

    @JsonCreator
    public HttpRequest(@JsonProperty("headers")Map<String, String> headers,
                       @JsonProperty("method") String method,
                       @JsonProperty("url") String url,
                       @JsonProperty("body") byte[] body) {
        this.headers = headers;
        this.method = method;
        this.url = url;
        this.body = body;
    }
}