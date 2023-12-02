package de.hellfish.ngrok.server;

import de.hellfish.ngrok.utils.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final ClientConnectionService clientList;

    /**
     * Handles incoming user requests to proxy them to the appropriate service.
     * <p>
     * This method is invoked for all requests with a path matching the pattern "/*" and is responsible for
     * forwarding the client request to the corresponding service. It retrieves the client's URL, looks up the
     * associated socket, and sends the HTTP request to the service via the socket.
     * Example of user-link - http://sub1.localhost:9000/index/api
     *
     * @param request The HttpServletRequest representing the incoming client request.
     * @return A ResponseEntity with the appropriate HTTP status code and response body from service.
     */
    @RequestMapping("/**")
    public ResponseEntity<String> userRequests(HttpServletRequest request) {

        String userUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        Socket clientSocket = clientList.getClient(userUrl);
        if (clientSocket == null) {
            log.warn("Link {} was not recognized by ngrok-server", userUrl);
            return ResponseEntity.badRequest().body(String.format("Link %s was not recognized by ngrok-server",
                    userUrl));
        }
        HttpRequest httpRequest = HttpRequestConverter.convertServletToNgrok(request);
        log.info("Server received user request: {}", httpRequest);

        try {
            HttpRequestSerializer.writeToOutputStream(httpRequest, clientSocket.getOutputStream());
            SerializableHttpResponse response = HttpResponseSerializer.readFromInputStream(clientSocket.getInputStream());
            return convertToResponseEntity(response);
        } catch (IOException e) {
            log.error("Error connection to client", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Error connection to client");
        }
    }

    private static ResponseEntity<String> convertToResponseEntity(SerializableHttpResponse serializedResponse) {
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(serializedResponse.getStatusCode());
        if (serializedResponse.getHeaders() != null) {
            for (Map.Entry<String, List<String>> entry : serializedResponse.getHeaders().entrySet()) {
                List<String> headerValues = entry.getValue();
                if (headerValues != null) {
                    for (String value : headerValues) {
                        responseBuilder.header(entry.getKey(), value);
                    }
                }
            }
        }
        return responseBuilder.body(serializedResponse.getBody());
    }
}