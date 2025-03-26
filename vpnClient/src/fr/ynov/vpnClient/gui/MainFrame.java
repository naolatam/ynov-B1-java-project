package fr.ynov.vpnClient.gui;

import fr.ynov.vpnClient.model.ClientSocket;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private final LoginPanel lp = new LoginPanel(this);
    private final MainPanel mp = new MainPanel();
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
        mainPanel.add(lp, "login");
        mainPanel.add(mp, "main");
        add(mainPanel);
    }

    public void showLoginPanel() {
        setTitle("Login");
        cl.show(mainPanel, "login");
    }
    public void showMainPanel() {
        setTitle(title);
        cl.show(mainPanel,"main");
    }
    public void addSocket(ClientSocket cs) {
        mp.addClient(cs);
        this.csList.add(cs);
    }

    public void closeSocket(ClientSocket cs) {
        try {
            this.csList.remove(cs);
            cs.close();
        } catch (IOException e) {
            this.csList.add(cs);
        }
    }


}
