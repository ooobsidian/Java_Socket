import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * @program: myChat
 * @description: Socket客户端
 * @author: obsidian
 * @create: 2018-09-13 00:10
 */
public class Client {
    private Socket socket;

    public Client() {
        try {
            socket = new Socket("localhost", 8080);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            OutputStream outputStream = socket.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
            PrintWriter printWriter = new PrintWriter(outputStreamWriter, true);
            Scanner scanner = new Scanner(System.in);
            while (true) {
                printWriter.println(scanner.nextLine());
                System.out.println("服务器发来一条消息: " + bufferedReader.readLine());
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String... args) {
        Client client = new Client();
        client.run();
    }
}
