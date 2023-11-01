package de.hellfish.ngrok.server;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class ClientMethodAndPort {
    private int port;
    private String method;

    @Override
    public String toString() {
        return method + " " + port;
    }

}
