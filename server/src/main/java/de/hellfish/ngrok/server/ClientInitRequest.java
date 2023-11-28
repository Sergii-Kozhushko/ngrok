package de.hellfish.ngrok.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class ClientInitRequest {

    private final int port;
    private final String protocol;

    public String getValue() {
        return protocol + " " + port;
    }
}