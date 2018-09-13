import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * @program: myChat
 * @description: Socket服务端
 * @author: obsidian
 * @create: 2018-09-13 00:01
 */
public class Server {
    private ServerSocket serverSocket;

    public Server() {
        try {
            serverSocket = new ServerSocket(8080);
            System.out.println("服务程序正在监听端口:" + serverSocket.getLocalPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            System.out.println("等待客户端连接...");
            Socket socket = serverSocket.accept();
            System.out.println("客户端已连接");
            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            OutputStream outputStream = socket.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
            PrintWriter printWriter = new PrintWriter(outputStreamWriter, true);
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("客户端发来一条消息: " + bufferedReader.readLine());
                printWriter.println(scanner.nextLine());
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String... args) throws IOException {
        Server server = new Server();
        server.run();
//        BufferedReader bufferedReader = new BufferedReader(
//                new InputStreamReader(System.in));
//
//        System.out.print("请输入一系列文字，可包括空格：");
//        String text = bufferedReader.readLine();
//        System.out.println("请输入文字：" + text);
    }
}
