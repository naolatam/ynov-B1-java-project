package fr.ynov.vpnClient.gui;

import fr.ynov.vpnClient.model.ClientSocket;

import javax.swing.*;
import java.awt.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private final LoginPanel lp = new LoginPanel(this);
    private final LoginPanel lp2 = new LoginPanel(this);
    private final String title;
    private CardLayout cl;
    private JPanel mainPanel;

    private List<ClientSocket> csList = new ArrayList<>();

    public MainFrame()  {
        super();
        this.title = "VPN Client";
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

        if(csList.isEmpty()) {
            showLoginPanel();
        }
        add(mainPanel);
    }

    public void showLoginPanel() {
        setTitle("Login");
        mainPanel.add(lp, "Login");
    }
    public void showMainPanel() {
        setTitle(title);
        mainPanel.add(lp2, "Login");
    }
    public void addSocket(ClientSocket cs) {
        cs.askServerKey();
        this.csList.add(cs);
    }

}
