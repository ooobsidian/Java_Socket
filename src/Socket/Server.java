package Socket;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
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

    // 存放客户端之间私聊的信息
    private Map<String, PrintWriter> stringPrintWriterMap;
    static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式

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
        System.out.println("【" + df.format(new Date()) + "】 " + "当前在线人数为: " + (stringPrintWriterMap.size()));
    }

    // 将给定的消息转发给所有客户端
    public synchronized void sendMessageToAll(String message) {
        for (PrintWriter out : stringPrintWriterMap.values()) {
            out.println(message);
        }
    }

    // 将给定的消息转发给私聊的客户端
    private synchronized void sendMessageToSomeone(String name, String message) {
        PrintWriter printWriter = stringPrintWriterMap.get(name);
        if (printWriter != null) {
            printWriter.println(message);
        } else if (printWriter == null) {
            JOptionPane.showMessageDialog(null, "用户\"" + name + "\"不在线或不存在!", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void start() {
        try {

            while (true) {
                System.out.println("【" + df.format(new Date()) + "】 " + "等待客户端连接...");
                Socket socket = serverSocket.accept();
                // 获取客户端的ip
                InetAddress inetAddress = socket.getInetAddress();
                System.out.println("【" + df.format(new Date()) + "】 " + "客户端:" + inetAddress.getHostAddress() + "上线!");
                System.out.println("【" + df.format(new Date()) + "】 " + "当前聊天室在线人数为: " + (stringPrintWriterMap.size() + 1));
                executorService.execute(new ListenerClient(socket)); //通过线程池来分配线程
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 该线程体用来处理给定的某一个客户端的消息，循环接收客户端发送
     * 的每一个字符串，并输出到控制台
     */
    class ListenerClient implements Runnable {
        private Socket socket;
        private String name;

        public ListenerClient(Socket socket) {
            this.socket = socket;
        }

        private String getName() throws Exception {
            try {
                // 服务端的输入读取客户端发送的昵称输出流
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                // 服务端将昵称验证结果通过自身输出流发送给客户端
                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
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
                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                //将客户昵称和其所发送的内容存在共享集合HashMap中
                name = getName();
                addClient(name, printWriter);
                Thread.sleep(100);
                sendMessageToAll("【" + df.format(new Date()) + "】 " + "[系统通知]: " + name + "已上线!");
                // 读入服务端消息
                //TODO 无法读入服务端输入,读入之后就会和其他东西冲突,把原来if改成while能循环读入服务器的消息,但是会将客户端卡死
                // 通过客户端的Socket获取输入流
                // 读取客户端发送来的信息
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
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
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                remove(name);
                // 通知所有用户,某用户已经下线
                sendMessageToAll("【" + df.format(new Date()) + "】 " + "[系统通知]: " + name + "已下线!");
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

    public static void main(String... args) {
        Server server = new Server();
        server.start();
    }
}
