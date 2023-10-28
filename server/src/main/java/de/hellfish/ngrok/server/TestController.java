package de.hellfish.ngrok.server;

import de.hellfish.ngrok.utils.HttpRequest;
import de.hellfish.ngrok.utils.HttpRequestConverter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;



@RestController

public class TestController {
    @GetMapping("/**")
    public String index(HttpServletRequest request) throws IOException {
        // сюда залетают все запросы
        // разбираем запросы юзеров по поддоменам


        HttpRequest result = HttpRequestConverter.convert(request);
        System.out.println(result);

        return request.getHeader("host");
    }

}
