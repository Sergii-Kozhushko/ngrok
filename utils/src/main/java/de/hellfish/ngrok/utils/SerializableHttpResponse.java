package de.hellfish.ngrok.utils;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@NoArgsConstructor
public final class SerializableHttpResponse {
    private int statusCode;
    private Map<String, List<String>> headers;
    private String body;

    public SerializableHttpResponse(HttpResponse<String> response) {
        this.statusCode = response.statusCode();
        this.headers = response.headers().map();
        this.body = response.body();
    }

    public SerializableHttpResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SerializableHttpResponse that = (SerializableHttpResponse) o;
        return statusCode == that.statusCode && Objects.equals(headers, that.headers) && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusCode, headers, body);
    }
}