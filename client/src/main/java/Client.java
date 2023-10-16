import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    private static final int clientPort = 8082;
    private static int servicePort = 2222;
    private static final String serverHost = "localhost";
    private static final int serverPort = 8082;
    private static boolean running = true;

    public static void main(String[] args) {
        // аргумент строки запуска -service-port=номер_порта сервиса
        for (String arg : args) {
            if (arg.startsWith("-service-port")) {
                servicePort = Integer.parseInt(arg.substring("-service-port=".length()));
            }
        }

        try  {
            Socket socket = new Socket(serverHost, serverPort);
            PrintWriter serverOut = new PrintWriter(socket.getOutputStream());
            BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            serverOut.print("HTTP " + servicePort +"\n");
            serverOut.flush();
            String line;
            StringBuilder request = new StringBuilder();
            while (!serverIn.ready()) ;
            String serverReply = serverIn.readLine();

            // сервер прислал ошибку
            if (serverReply.split(" ")[0].equals("ERROR")) {
                System.out.println(serverReply.substring("ERROR ".length()));
                running = false;
            } else {
                String link = serverReply.split(" ")[1];
                System.out.println("Link for user requests: http://localhost:" + servicePort + " -> " + link);
            }

            while (running) {
                while (!serverIn.ready()) ;
                while ((line = serverIn.readLine()) != null && !line.isEmpty()) {
                    request.append(line).append("\r\n");
                }
                System.out.println("Client got message from server: \n" + request);
                // получить номер юзера
                String userNumber = request.toString().split("\r\n")[0].split(" ")[1];
                String message = request.toString().substring(("USER " + userNumber+"\r\n").length());


                // переслать полученное сообщение сервису и получить ответ
                StringBuilder responseFromService = exchangePacketsWithService(message);

                System.out.println("Client got response from service:\n" + responseFromService);

                // переслать ответ сервиса серверу
                String response = "USER " + userNumber + "\r\n" + responseFromService + "\n";
                serverOut.print(response);
                serverOut.flush();



            }

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println("Server is not reachable on " + serverHost + ":" + serverPort);
            e.printStackTrace();
        }
    }




    private static StringBuilder exchangePacketsWithService(String request) {
        StringBuilder response = new StringBuilder();
        try

        {
            Socket socket = new Socket("localhost", servicePort);
            // поток для отправки запроса на сервис
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //отправить запрос сервису
            out.print(request + "\n");
            out.flush();

            // принимаем ответ
            String line;
            while ((line = in.readLine()) != null ) {
                response.append(line).append("\r\n");
            }

        } catch (
                IOException ex) {
            ex.printStackTrace();
        }
        return response;
    }




}
