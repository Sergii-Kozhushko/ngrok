import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TestService {
    private static final int testServicePort = 2222;
    private static final int clientPort = 8082;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(testServicePort)) {
            System.out.println("Test Service started!");
            while (true) {
                Socket socket = serverSocket.accept();
                // пришел запрос на тунель
                System.out.print("Someone connected. ");
                System.out.println(" Host: " + socket.getLocalAddress() + ":" + socket.getPort());

                try (
                        // поток чтения запроса от клиента
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(socket.getInputStream(),
                                        StandardCharsets.UTF_8));
                        //  поток ответа
                        PrintWriter writer = new PrintWriter(socket.getOutputStream())
                ) {

                    // ждем входящие данные
                    while (!reader.ready()) ;
                    // читаем запрос
                    StringBuilder request = new StringBuilder();
                    while (reader.ready()) {
                        request.append(reader.readLine());
                        request.append("\r\n");

                    }
                    System.out.println(request);




                    // отправить ответ клиенту
                    writer.println("HTTP/1.1 404 NOT ACCEPTABLE");
                    writer.println("Content-Type: text/html; charset=utf-8");
                    writer.print("\r\nBody");
                    writer.println();
                    writer.flush();
                    // здесь надо соедениться с клиентом ngrok и передать ему запрос

                }
            }

        } catch (
                IOException ex) {
            ex.printStackTrace();
        }
    }



}
