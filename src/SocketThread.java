import java.io.*;
import java.net.Socket;

/**
 * @program: Java_Socket
 * @description:
 * @author: obsidian
 * @create: 2018-09-13 10:16
 */
public class SocketThread extends Thread {
    private Socket socket;

    public SocketThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String temp = null;
            String info = "";
            while ((temp = bufferedReader.readLine()) != null) {
                info += temp;
                System.out.println("已收到服务器连接");
                System.out.println("服务端接收到客户端信息：" + info + ",当前客户端ip为：" + socket.getInetAddress().getHostAddress());
            }
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
            printWriter.print("你好,服务器已收到你的消息");
            printWriter.print(info);
            printWriter.flush();
            socket.shutdownInput();
//            bufferedReader.close();
//            inputStream.close();
//            printWriter.close();
//            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
