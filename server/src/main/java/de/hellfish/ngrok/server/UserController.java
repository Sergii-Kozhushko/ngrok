package de.hellfish.ngrok.server;

import de.hellfish.ngrok.utils.HttpRequestConverter;
import de.hellfish.ngrok.utils.HttpRequestSerializer;
import de.hellfish.ngrok.utils.MyHttpRequest;
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
        if (!clientList.containsUserLink(userURL)) {
            log.warn("Server received wrong link from user: " + userURL);
            return ResponseEntity.badRequest().body("Link " + userURL +" was not recognized by ngrok-server");
        }
        Socket clientSocket = clientList.getSocketByUserLink(userURL);
        MyHttpRequest myHttpRequest = HttpRequestConverter.convertServletToMy(request);
        log.info("Server received user request: " + myHttpRequest);

        try {
            HttpRequestSerializer.writeToOutputStream(myHttpRequest, clientSocket.getOutputStream());
        } catch (IOException e) {
            log.error("Error sending user request to client", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Error connecting to ngrok-client");
        }
        return ResponseEntity.ok().build();
    }

}