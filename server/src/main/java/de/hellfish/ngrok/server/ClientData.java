package de.hellfish.ngrok.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.net.Socket;

@Getter
@RequiredArgsConstructor
public final class ClientData {
    private final String userLink;
    private final Socket socket;
    private final String userProtocol;
    private final int userPort;

    public String getServiceProtocolAndPort() {
        return userProtocol + " " + userPort;
    }

    public String getClientHost() {
        return socket.getLocalAddress() + ":" + socket.getPort();
    }
}
