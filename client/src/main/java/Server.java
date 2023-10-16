import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int serverPort = 8081;
    private static final int clientPort = 8082;

    public static void main(String[] args) {
        // поток для туннеля
        new Thread(new ServerTunnel()).start();
    }

    static class ServerTunnel implements Runnable {

        @Override
        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
                System.out.println("Server started!");
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.print("New client connected. ");
                    System.out.println(" Host: " + clientSocket.getLocalAddress() + ":" + clientSocket.getPort());
                    // Создаем поток для обработки запроса

                    new Thread(new ClientHandler(clientSocket)).start();

                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class ClientHandler implements Runnable {
        private Socket client;

        // Конструктор с параметром сокета
        public ClientHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                // Получаем входной и выходной потоки для чтения и записи данных
                BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter output = new PrintWriter(client.getOutputStream(), true);

                StringBuilder request = new StringBuilder();
                String line;
                while ((line = input.readLine()) != null && !line.isEmpty()) {
                    request.append(line).append("\r\n");
                }


                System.out.println(request);
                // отправить полученный запрос на клиент и ждать ответа
                var response = requestToClient(request);

                System.out.println("Server sends response to browser");
                output.write(response.toString());
                output.flush();


// Закрываем потоки и соединение
                output.close();
                input.close();
                client.close();
                System.out.println("Client thread is closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }





    private static StringBuilder requestToClient(StringBuilder request) {
        StringBuilder response = new StringBuilder();
        try

        {
            Socket socket = new Socket("localhost", clientPort);
            // поток для отправки запроса на клиент
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Server send request to Client");
            out.print(request.toString() + "\r\n");
            out.flush();

            // принимаем ответ
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                response.append(line).append("\r\n");
            }
            System.out.println("Server got response from client");

            out.close();
            in.close();
            socket.close();


        } catch (
                IOException ex) {
            ex.printStackTrace();
        }
        return response;
    }


}
