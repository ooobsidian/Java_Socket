
import javax.swing.*;
import java.net.URL;

/**
 * @program: Java_Socket
 * @description:
 * @author: obsidian
 * @create: 2018-09-14 21:04
 */
public class Login extends JFrame {
    private static final long serialVersionUID = -5665975170821790753L;

    public Login() {
        initComponent();
    }

    private void initComponent() {
        setTitle("用户登录");
        // LOGO
//        URL image = Login.class.getClassLoader().getResource()
//        JLabel imageLogo = new JLabel(new ImageIcon(image));
        JPanel jPanel = new JPanel();
        JPanel jPanelAccount = new JPanel();
        jPanelAccount.add(new JLabel("账号"));
        JTextField userTextField = new JTextField(15);
        jPanelAccount.add(userTextField);
        jPanelAccount.add(new JLabel("注册账号"));
        jPanel.add(jPanelAccount);

        JPanel jPanelPass = new JPanel();
        jPanelPass.add(new JLabel("密码"));
        JPasswordField passwordField = new JPasswordField(15);
        jPanelPass.add(passwordField);
        jPanel.add(jPanelPass);

        // 登录设置
        JPanel jPanelStatus = new JPanel();
        jPanelStatus.add(new JLabel("状态"));
        JComboBox statusComboBox = new JComboBox();
        statusComboBox.addItem("Q我");
        statusComboBox.addItem("在线");
        statusComboBox.addItem("隐身");
        statusComboBox.addItem("离线");
        jPanelStatus.add(statusComboBox);
        jPanelStatus.add(new JCheckBox("记住密码"));
        jPanelStatus.add(new JCheckBox("自动登录"));
        jPanel.add(jPanelStatus);
        add(jPanel);



    }
}
