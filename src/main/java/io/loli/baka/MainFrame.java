package io.loli.baka;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = -971359179534076053L;

    private JButton addButton = new JButton("添加账号");

    private Executor service = Executors.newCachedThreadPool();

    private void init() {
        this.add(addButton);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Hashtable<String, String> auth = login(MainFrame.this);

                JTextArea area = new JTextArea();
                UserAction action = new UserAction(auth.get("user"), auth.get("pass"), new TextAreaMessageSender(area));
                add(new UserPanel(action, MainFrame.this, area));

                service.execute(action);

                revalidate();
                repaint();

            }
        });

        this.revalidate();
        this.repaint();
    }

    public MainFrame() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(300, 500);
        this.setTitle("苍雪机器人");
        this.setVisible(true);
        this.setResizable(true);
        this.setLayout(new FlowLayout());
        this.init();

    }

    public static void main(String[] args) {
        new MainFrame();
    }

    public Hashtable<String, String> login(JFrame frame) {
        Hashtable<String, String> logininformation = new Hashtable<String, String>();
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JPanel label = new JPanel(new GridLayout(0, 1, 2, 2));
        label.add(new JLabel("用户名", SwingConstants.RIGHT));
        label.add(new JLabel("密码", SwingConstants.RIGHT));
        panel.add(label, BorderLayout.WEST);

        JPanel controls = new JPanel(new GridLayout(0, 1, 2, 2));
        JTextField username = new JTextField(10);
        username.requestFocus();
        controls.add(username);
        JPasswordField password = new JPasswordField(10);
        controls.add(password);
        panel.add(controls, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(frame, panel, "输入登陆信息", JOptionPane.QUESTION_MESSAGE);
        logininformation.put("user", username.getText());
        logininformation.put("pass", new String(password.getPassword()));
        return logininformation;
    }

    class UserPanel extends JPanel {
        private static final long serialVersionUID = -865100404392863370L;
        private UserAction user;

        private JLabel nameLabel = null;
        private JButton logButton = new JButton("日志");
        private JButton delete = new JButton("停止并删除");

        private JTextArea logArea;

        private JFrame parent = null;

        private void init() {
            nameLabel = new JLabel(user.getUserName());
            this.add(nameLabel);
            this.add(logButton);
            this.add(delete);
            logButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    JDialog dialog = new JDialog(parent);
                    dialog.setLocationByPlatform(true);
                    logArea.setAutoscrolls(true);
                    //logArea.setPreferredSize(new Dimension(300, 400));
                    logArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    logArea.setFont(new Font("courier new", Font.PLAIN, 12));
                    logArea.setLineWrap(true);
                    JScrollPane txtAreaScroll = new JScrollPane();
                    txtAreaScroll.setViewportView(logArea);
                    txtAreaScroll.setAutoscrolls(true);
                    dialog.add(txtAreaScroll);
                    dialog.setSize(300, 400);
                    dialog.setPreferredSize(new Dimension(300, 400));
                    dialog.pack();
                    dialog.setVisible(true);
                }
            });

            delete.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    user.close();
                    parent.remove(UserPanel.this);
                    parent.revalidate();
                    parent.repaint();

                }
            });

            logArea.setLineWrap(true);// 设置自动换行，之后则不需要设置水平滚动条
            JScrollPane scroll = new JScrollPane(logArea);
            scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        }

        public UserPanel(UserAction user, JFrame jframe, JTextArea area) {
            this.logArea = area;
            this.user = user;
            this.parent = jframe;
            this.setSize(new Dimension(300, 100));
            this.setPreferredSize(new Dimension(300, 30));
            this.setLayout(new FlowLayout());

            init();
        }

        public JTextArea getLogArea() {
            return logArea;
        }

    }
}
