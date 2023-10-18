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
            System.out.println("Test Service started! Listening on: " + testServicePort);
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
                    System.out.println("TestService got request:\n" + request);

                    String reply200 = "HTTP/1.1 200 OK\r\n" +
                            "Date: Fri, 14 Oct 2023 10:00:00 GMT\r\n" +
                            "Server: Apache/2.4.41 (Unix)\r\n" +
                            "Content-Length: 4\r\n" +
                            "Content-Type: text/plain; charset=utf-8" +
                            "\r\n\r\n" +
                            "test";

                    writer.write(reply200);
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
