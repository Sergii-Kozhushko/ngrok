package de.hellfish.ngrok.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hellfish.ngrok.utils.HttpRequestConverter;
import de.hellfish.ngrok.utils.HttpRequestSerializer;
import de.hellfish.ngrok.utils.MyHttpRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.Socket;
import java.net.http.HttpResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {
    private final Clients clientList;

    @GetMapping("/**")
    public ResponseEntity<String> clientRequests(HttpServletRequest request) {

        String baseURL = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        if (!clientList.getList().containsKey(baseURL)) {
            return ResponseEntity.badRequest().body("Link not recognized by ngrok-server");
        }
        // отправить сериализованный запрос клиенту
        Socket clientSocket = clientList.getList().get(baseURL);
        MyHttpRequest myHttpRequest = HttpRequestConverter.convertServletToMy(request);

        HttpRequestSerializer.writeToOutputStream(myHttpRequest, clientSocket);
        // read response
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            HttpResponse<String> response = objectMapper.readValue(clientSocket.getInputStream(), HttpResponse.class);
            ResponseEntity<String> responseEntity = ResponseEntity.status(response.statusCode())
                    .body(response.body());
            return responseEntity;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // проверить что в мапе есть такой адрес

        //  получить ответ
        // вернуть ответ клиенту
        //return ResponseEntity.ok().build();
    }

}
