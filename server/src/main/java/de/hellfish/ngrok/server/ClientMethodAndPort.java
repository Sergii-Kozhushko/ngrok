package de.hellfish.ngrok.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
public class ClientMethodAndPort {
    private int port;
    private String method;

    @Override
    public String toString() {
        return method + " " + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientMethodAndPort that = (ClientMethodAndPort) o;
        if (method == null || that.method == null) return false;
        return port == that.port && method.equals(that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(port, method);
    }
}
