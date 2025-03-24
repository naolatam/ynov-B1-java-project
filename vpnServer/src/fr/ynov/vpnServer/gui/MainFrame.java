package fr.ynov.vpnServer.gui;

import javax.swing.*;
import java.awt.*;
import java.net.ServerSocket;

public class MainFrame  extends JFrame {
    private ServerSocket ss = null;
    private final SetupPanel sp = new SetupPanel(this);
    private final String title;
    private CardLayout cl;
    private JPanel mainPanel;

    public MainFrame() {
        super();
        this.title = "Server app";
        setTitle(title);
        init();
    }
    public MainFrame(String title) {
        super(title);
        this.title = title;
        init();
    }

    private void init() {
        setTitle(title);

        getContentPane().removeAll();
        cl = new CardLayout();
        mainPanel = new JPanel(cl);

        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if(ss == null) {
            showSetupPanel();
        }
        add(mainPanel);
    }

    private void showSetupPanel() {
        mainPanel.add(sp, "setup");
    }
    public void showMainPanel() {
        mainPanel.add(sp, "setup");
    }

    public void setServerSocket(ServerSocket ss) {
        this.ss = ss;
    }


}
