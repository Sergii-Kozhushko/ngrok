import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TestBrowser {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9001);
        boolean isRunning = true;
        int counter = 0;
        while (isRunning) {
            Socket socket = serverSocket.accept();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(),
                            StandardCharsets.UTF_8));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            String reply200 = "HTTP/1.1 200 OK\r\n" +
                    "Date: Fri, 14 Oct 2023 10:00:00 GMT\r\n" +
                    "Server: Apache/2.4.41 (Unix)\r\n" +
                    "Content-Length: 4\r\n" +
                    "Content-Type: text/plain; charset=utf-8" +
                    "\r\n\r\n" +
                    "test";
            writer.write(reply200);
            writer.flush();
            counter++;
            if (counter == 3) isRunning = false;
        }

    }

}
