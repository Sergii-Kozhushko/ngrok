import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {

    private static final int clientPort = 8082;
    private static int servicePort = 2222;

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
                new ClientHandler(client).start();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static class ClientHandler extends Thread {
        private Socket serverSocket;

        // Конструктор с параметром сокета
        public ClientHandler(Socket client) {
            this.serverSocket = client;
        }

        public void run() {
            try {
                // Получаем входной поток для чтения данных от сервера
                BufferedReader input = new BufferedReader(
                        new InputStreamReader(serverSocket.getInputStream(),
                                StandardCharsets.UTF_8));

                // читаем запрос от сервера и сразу передаем запрос сервису
                Socket serviceSocket = new Socket("localhost", servicePort);
                OutputStream serviceOutStream = serviceSocket.getOutputStream();

                String line;

                while (!input.ready()) ;
                while (input.ready()) {
                    line = input.readLine();
                    System.out.println(line);
                    serviceOutStream.write(line.getBytes());
                }
//                while ((line = input.readLine()) != null && !line.isEmpty()) {
//                    System.out.println(line);
//                    serviceOutStream.write(line.getBytes());
//                }
                serviceOutStream.flush();

                // читаем ответ от сервиса
                InputStream serviceInputStream = serviceSocket.getInputStream();
                BufferedReader serviceResponseReader = new BufferedReader(new InputStreamReader(serviceInputStream));

                // отправляем ответ обратно серверу
                OutputStream output = serviceSocket.getOutputStream();
                String responseLine;
                while ((responseLine = serviceResponseReader.readLine()) != null) {
                    output.write(responseLine.getBytes());
                }
                output.flush();

                // Закрываем потоки и соединение
                output.close();
                input.close();
                serviceInputStream.close();
                serviceSocket.close();
                serviceResponseReader.close();
                System.out.println("Client thread is closed");
            } catch (ConnectException e) {
                System.out.println("Service is not available at localhost:" + servicePort);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}




