package de.hellfish.ngrok.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.net.Socket;

@Getter
@RequiredArgsConstructor
public final class SocketState {
    private final Socket socket;
    private final String serviceProtocol;
    private final int servicePort;
}