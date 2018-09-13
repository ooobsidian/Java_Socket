import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @program: myChat
 * @description: Socket客户端
 * @author: obsidian
 * @create: 2018-09-13 00:10
 */
public class Client {
    static private Socket socket;

    public Client() {
    }

    public static void main(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String serverIp;
        System.out.println("请设置服务端IP: ");
        serverIp = scanner.next();
        socket = new Socket(serverIp, 8080);
        Client client = new Client();
        client.run();
    }

    public void run() {
        try {
            Scanner scanner = new Scanner(System.in);
            setName(scanner);
            // 接收服务器端发送过来的信息的线程启动
            ExecutorService executorService = Executors.newCachedThreadPool();
            ListenerServser listenerServser = new ListenerServser();
            executorService.execute(listenerServser);
            // 建立输出流,向服务端发送信息
            OutputStream outputStream = socket.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
            PrintWriter printWriter = new PrintWriter(outputStreamWriter);
            while (true) {
                printWriter.println(scanner.nextLine());
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
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

    private void setName(Scanner scanner) throws Exception {
        String name;
        // 创建输出流
        OutputStream outputStream = socket.getOutputStream();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
        PrintWriter printWriter = new PrintWriter(outputStreamWriter, true);
        // 创建输入流
        InputStream inputStream = socket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        while (true) {
            System.out.println("请创建你的昵称: ");
            name = scanner.nextLine();
            if (name.trim().equals("")) {
                System.out.println("昵称不得为空!");
            } else {
                printWriter.println(name);
                String pass = bufferedReader.readLine();
                if (pass != null && (!pass.equals("OK"))) {
                    System.out.println("该昵称已经被占用,请重新输入: ");
                } else {
                    System.out.println("昵称: \"" + name + "\"已设置成功,现在可以开始聊天啦!");
                    break;
                }
            }
        }
    }

    // 循环读取服务端发送过来的信息并输出到客户端的控制台
    class ListenerServser implements Runnable {
        @Override
        public void run() {
            try {
                InputStream inputStream = socket.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String msgString;
                while ((msgString = bufferedReader.readLine()) != null) {
                    System.out.println(msgString);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


