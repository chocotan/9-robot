package io.loli.baka;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTextArea;

public class TextAreaMessageSender {

    private JTextArea text;
    private UserAction user;

    public TextAreaMessageSender(JTextArea text, UserAction user) {
        this.text = text;
        this.user = user;
    }

    public TextAreaMessageSender(JTextArea text) {
        this.text = text;
    }

    public void log(String log) {
        logStatic(text, user, "[INFO] " + log);
    }

    public void err(String log) {
        logStatic(text, user, "[ERROR] " + log);
    }

    public static void logStatic(JTextArea text, UserAction user, String log) {
        String t = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " [" + user.getUserName() + "]-"
            + log;
        System.out.println(t);
        text.append(t + "\n");
    }

    public void setUser(UserAction user) {
        this.user = user;
    }

}
