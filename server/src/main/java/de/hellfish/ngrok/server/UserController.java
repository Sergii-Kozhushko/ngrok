package de.hellfish.ngrok.server;

import de.hellfish.ngrok.utils.HttpRequestConverter;
import de.hellfish.ngrok.utils.HttpRequestSerializer;
import de.hellfish.ngrok.utils.HttpRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.net.Socket;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {
    private final ClientList clientList;

    /**
     * Method processes requests from users
     * @param request - contains all info about incoming request
     * @return
     */
    @GetMapping("/**")
    public ResponseEntity<String> clientRequests(HttpServletRequest request) {

        // http://sub1.localhost:9000/index/api
        String userURL = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        SocketState client = clientList.getList().get(userURL);
        if (client == null) {
            log.warn(String.format(Messages.ERROR_WRONG_LINK_USER,  userURL));
            return ResponseEntity.badRequest().body(String.format(Messages.ERROR_WRONG_LINK_USER,  userURL));
        }
        Socket clientSocket = client.getSocket();
        HttpRequest httpRequest = HttpRequestConverter.convert(request);
        log.info(String.format(Messages.NEW_USER_REQUEST, httpRequest));

        try {
            HttpRequestSerializer.writeToOutputStream(httpRequest, clientSocket.getOutputStream());
        } catch (IOException e) {
            log.error(Messages.ERROR_CONNECTION_CLIENT, e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Messages.ERROR_CONNECTION_CLIENT);
        }
        return ResponseEntity.ok().build();
    }
}