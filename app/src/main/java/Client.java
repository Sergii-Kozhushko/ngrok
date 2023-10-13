import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Client {

    private static final int clientPort = 8082;
    private static int servicePort = 80;

    public static void main(String[] args) {
        // аргумент строки запуска -service-port=номер_порта сервиса
        for (String arg : args) {
            if (arg.startsWith("-service-port")) {
                servicePort = Integer.parseInt(arg.substring("-service-port=".length()));
            }
        }
        try {
            ServerSocket serverSocket = new ServerSocket(clientPort);
            System.out.println("Client started!");
            while (true) {
                Socket client = serverSocket.accept();
                System.out.print("New request from server. ");
                System.out.println(" Host: " + client.getLocalAddress() + ":" + client.getPort());
                // Создаем поток для обработки запроса
                new Thread(new ClientHandler(client)).start();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static class ClientHandler implements Runnable {
        private Socket client;

        // Конструктор с параметром сокета
        public ClientHandler(Socket client) {
            this.client = client;
        }

        public void run() {
            try {
                // Получаем входной и выходной потоки для чтения и записи данных
                BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter output = new PrintWriter(client.getOutputStream(), true);
                System.out.println("Client ready to read data from server");
                StringBuilder request = new StringBuilder();
                String line;
                while ((line = input.readLine()) != null && !line.isEmpty()) {
                    request.append(line).append("\r\n");
                }


                System.out.println(request);
                // отправить полученный запрос на сервис и ждать ответа
                var response = requestToService(request);

                System.out.println("Client sends response to Server");
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

    private static StringBuilder requestToService(StringBuilder request) {
        StringBuilder response = new StringBuilder();
        try

        {
            Socket socket = new Socket("localhost", servicePort);
            // поток для отправки запроса на сервис
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.print(request.toString());
            out.flush();

            // принимаем ответ
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                response.append(line).append("\r\n");
            }
            System.out.println("Client got response from service");

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




