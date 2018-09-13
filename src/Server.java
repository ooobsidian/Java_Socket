import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @program: myChat
 * @description: Socket服务端
 * @author: obsidian
 * @create: 2018-09-13 00:01
 */
public class Server {
    private ServerSocket serverSocket;

    private ExecutorService executorService;


    private Map<String, PrintWriter> stringPrintWriterMap;


    public Server() {
        try {
            serverSocket = new ServerSocket(8080);
            stringPrintWriterMap = new HashMap<>();
            executorService = Executors.newCachedThreadPool();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 将客户端的信息以map的形式存入集合中
    private void addClient(String key, PrintWriter value) {
        synchronized (this) {
            stringPrintWriterMap.put(key, value);
        }
    }

    // 将给点的输出流从共享集合中删除
    private synchronized void remove(String key) {
        stringPrintWriterMap.remove(key);
        System.out.println("当前在线人数为: " + (stringPrintWriterMap.size() + 1));
    }

    // 将给定的消息转发给所有客户端
    private synchronized void sendMessageToAll(String message) {
        for (PrintWriter out : stringPrintWriterMap.values()) {
            out.println(message);
        }
    }

    // 将给定的消息转发给私聊的客户端
    private synchronized void sendMessageToSomeone(String name, String message) {
        PrintWriter printWriter = stringPrintWriterMap.get(name);
        if (printWriter != null) {
            printWriter.println(message);
        }
    }

    public void run() {
        try {
            while (true) {
                System.out.println("等待客户端连接...");
                Socket socket = serverSocket.accept();
                // 获取客户端的ip
                InetAddress inetAddress = socket.getInetAddress();
                System.out.println("客户端:" + inetAddress.getHostAddress() + "上线!");
                System.out.println("当前聊天室在线人数为: " + (stringPrintWriterMap.size() + 1));
                executorService.execute(new ListenerClient(socket));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ListenerClient implements Runnable {
        private Socket socket;
        private String name;

        public ListenerClient(Socket socket) {
            this.socket = socket;
        }

        private String getName() throws Exception {
            try {
                // 服务端的输入读取客户端发送的昵称输出流
                InputStream inputStream = socket.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                // 服务端将昵称验证结果通过自身输出流发送给客户端
                OutputStream outputStream = socket.getOutputStream();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
                PrintWriter printWriter = new PrintWriter(outputStreamWriter, true);
                // 读取客户端发来的昵称
                while (true) {
                    String nameString = bufferedReader.readLine();
                    if ((nameString.trim().length() == 0) || stringPrintWriterMap.containsKey(nameString)) {
                        printWriter.println("FAIL");
                    } else {
                        printWriter.println("OK");
                        return nameString;
                    }
                }
            } catch (Exception e) {
                throw e;
            }
        }

        @Override
        public void run() {
            try {
                // 通过客户端的Socket获取客户端的输出流
                OutputStream outputStream = socket.getOutputStream();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
                PrintWriter printWriter = new PrintWriter(outputStreamWriter, true);
                //将客户昵称和其所发送的内容存在共享集合HashMap中
                name = getName();
                addClient(name, printWriter);
                Thread.sleep(100);
                sendMessageToAll("[系统通知]: " + name + "已上线!");
                // 通过客户端的Socket获取输入流
                // 读取客户端发送来的信息
                InputStream inputStream = socket.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String msgString = null;

                while ((msgString = bufferedReader.readLine()) != null) {
                    // 检验是否为私聊(格式: @昵称:内容)
                    if (msgString.startsWith("@")) {
                        int index = msgString.indexOf(":");
                        if (index >= 0) {
                            // 获取昵称
                            String theName = msgString.substring(1, index);
                            String info = msgString.substring(index + 1, msgString.length());
                            info = name + ": " + info;
                            // 将私聊信息发送出去
                            sendMessageToSomeone(theName, info);
                            continue;
                        }
                    }
                    // 遍历所有输出流,将该客户端发送的信息转发给所有客户端
                    System.out.println(name + ": " + msgString);
                    sendMessageToAll(name + ": " + msgString);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                remove(name);
                // 通知所有用户,某用户已经下线
                sendMessageToAll("[系统通知]: " + name + "已下线!");
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String... args) throws IOException {
        Server server = new Server();
        server.run();
    }
}
