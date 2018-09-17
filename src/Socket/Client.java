package Socket;

import javax.swing.*;
import javax.xml.soap.Text;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
/**
 * @program: myChat
 * @description: Socket客户端
 * @author: obsidian
 * @create: 2018-09-13 00:10
 */
public class Client extends JFrame {
    static private Socket socket;
    private TextField textField = new TextField();
    private TextArea textArea = new TextArea();
    private DataOutputStream dataOutputStream = null;

    public Client() {
    }

    public static void main(String... args) throws Exception {
        new Client().launcFrame();
    }

    //绘制聊天界面
    public void launcFrame() {
        setLocation(300, 400);
        this.setSize(200, 400);
        add(textField, BorderLayout.SOUTH);
        add(textArea, BorderLayout.NORTH);
        pack();
        // 监听图形界面窗口的关闭事件
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
//                disConnect();
                // TODO 断开连接
            }
        });
        textField.addActionListener(new TFLister());
    }


    //连接服务器
    public void connect() {
        Scanner scanner = new Scanner(System.in);
        String serverIp;
        System.out.println("请设置服务端IP: ");
        serverIp = scanner.next();
        try {
            socket = new Socket(serverIp, 8080);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendMessage() {
        // 建立输出流,向服务端发送信息
        Scanner scanner = new Scanner(System.in);
        OutputStream outputStream = null;
        try {
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        PrintWriter printWriter = new PrintWriter(outputStreamWriter,true);
        while (true) {
            printWriter.println(scanner.nextLine());
        }
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
//            OutputStream outputStream = socket.getOutputStream();
//            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
//            PrintWriter printWriter = new PrintWriter(outputStreamWriter,true);
//            while (true) {
//                printWriter.println(scanner.nextLine());
//            }
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

    private class TFLister implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            String text = textField.getText().trim();
            textArea.setText(text);
            textField.setText("");
            sendMessage();
        }
    }

}


