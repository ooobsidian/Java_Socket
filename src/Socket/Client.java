package Socket;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @program: myChat
 * @description: Socket客户端
 * @author: obsidian
 * @create: 2018-09-13 00:10
 */
public class Client extends JFrame {
    static private Socket socket;
    private static DataOutputStream dataOutputStream = null;
    static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
    static private boolean is_online = false;   //客户端是否在线标志
    static String userName;
    static TextArea ta;
    static TextField nameField;

    public static void setUserName(String userName) {
        Client.userName = userName;
    }

    public static String getUserName() {
        return userName;

    }

    public Client() {
    }


    public static void main(String... args) {
        JFrame f = new JFrame();
        f.setLayout(null);

        //连接服务器的按钮
        final Button BConnect = new Button("上线");
        BConnect.setBounds(10, 10, 100, 35);

        //断开服务器按钮
        final Button BDisConnect = new Button("下线");
        BDisConnect.setBounds(120, 10, 100, 35);
        BDisConnect.setEnabled(false);

        //当前用户昵称标签
        Label label=new Label("当前昵称: ");
        label.setBounds(400,13,70,30);

        //当前用户昵称
        nameField=new TextField();
        nameField.setEditable(false);
        nameField.setBackground(Color.white);
        nameField.setBounds(470,13,150,30);

        //系统消息框标签
        Label lb1 = new Label("----消息框----");
        lb1.setBounds(10, 55, 300, 20);

        //系统消息框
        ta = new TextArea();
        ta.setBackground(Color.LIGHT_GRAY);
        ta.setEditable(false);
        ta.setBounds(10, 80, 680, 400);


        //发送框
        final TextField tf = new TextField();
        tf.setEditable(true);
        tf.setBounds(10, 500, 550, 30);

        //发送按钮
        final Button Bsend = new Button("发送");
        Bsend.setBounds(590, 500, 80, 30);
        Bsend.setEnabled(false);

        //将组件加入框架
        f.add(BConnect);
        f.add(BDisConnect);
        f.add(label);
        f.add(nameField);
        f.add(lb1);
        f.add(ta);
        f.add(tf);
        f.add(Bsend);

        //设置框架属性
        f.setSize(700, 600);
        f.setVisible(true);
        f.setResizable(false);
        f.setTitle("iChat聊天室");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //连接服务器
        BConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (!is_online) {
                        Client client = new Client();
                        socket = new Socket("0.0.0.0", 8080);
                        String userName = setName();
                        setUserName(userName);
                        dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        JOptionPane.showConfirmDialog(null, "登录成功!", "登录提示", JOptionPane.YES_NO_OPTION);
                        System.out.println("登录成功!");
                        nameField.setText(userName);
                        BDisConnect.setEnabled(true);
                        Bsend.setEnabled(true);
                        is_online = true; // 设置该客户端已经在线
                        client.start();
                    } else {
                        JOptionPane.showMessageDialog(null, "同一客户端不能重复登录!", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SocketTimeoutException e2) {
                    JOptionPane.showMessageDialog(null, "登录超时!", "登录提示", JOptionPane.ERROR_MESSAGE);
                } catch (UnknownHostException e1) {
                    JOptionPane.showMessageDialog(null, "登录超时!", "登录提示", JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(null, "登录失败!", "登录提示", JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        //断开服务器
        BDisConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    dataOutputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                ta.append("【" + df.format(new Date()) + "】\n" + userName + "下线!\n");
                nameField.setText("");
                BDisConnect.setEnabled(false);
                Bsend.setEnabled(false);
                is_online = false;
            }
        });

        // 发送按钮功能
        Bsend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = tf.getText().trim();
                if (message.length() == 0) {
                    JOptionPane.showMessageDialog(null, "发送消息不能为空!", "错误", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        sendMessage(message);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    tf.setText(null);  //清空输入框
                }
            }
        });

    }

    public static void sendMessage(String message) throws IOException {
        // 建立输出流,向服务端发送信息
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        printWriter.println(message);
    }


    public void start() {
        try {
            ExecutorService executorService = Executors.newCachedThreadPool();
            executorService.execute(new ListenerServser());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String setName() throws Exception {
        String name;
        // 创建输出流
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        // 创建输入流
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        while (true) {
            System.out.println("请创建你的昵称: ");
            name = JOptionPane.showInputDialog("请创建你的昵称");
            if (name.trim().equals("")) {
                System.out.println("昵称不得为空!");
                JOptionPane.showMessageDialog(null, "昵称不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            } else {
                printWriter.println(name);
                String pass = bufferedReader.readLine();
                if (pass != null && (!pass.equals("OK"))) {
                    JOptionPane.showMessageDialog(null, "该昵称已经被占用,请重新输入", "错误", JOptionPane.ERROR_MESSAGE);
                    System.out.println("该昵称已经被占用,请重新输入: ");
                } else {
                    JOptionPane.showMessageDialog(null, "昵称: \"" + name + "\"已设置成功,现在可以开始聊天啦!", "成功", JOptionPane.YES_NO_CANCEL_OPTION);
                    System.out.println("昵称: \"" + name + "\"已设置成功,现在可以开始聊天啦!");
                    break;
                }
            }
        }
        return name;
    }


    // 循环读取服务端发送过来的信息并输出到客户端的控制台
    class ListenerServser implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                String msgString;
                while ((msgString = bufferedReader.readLine()) != null) {
                    if (msgString != "")
                        ta.append("【" + df.format(new Date()) + "】\n" + msgString + "\n");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


