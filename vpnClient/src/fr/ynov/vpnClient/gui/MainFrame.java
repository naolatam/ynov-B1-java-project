package fr.ynov.vpnClient.gui;

import fr.ynov.vpnClient.model.ClientSocket;
import fr.ynov.vpnModel.model.ConfigurationMessage;
import fr.ynov.vpnModel.model.Message;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private static final String LOGIN_PANEL = "login";
    private static final String MAIN_PANEL = "main";


    private final LoginPanel lp = new LoginPanel(this);
    private final MainPanel mp = new MainPanel(this);
    private final String title;
    private CardLayout cl;
    private JPanel mainPanel;

    private final List<ClientSocket> csList = new ArrayList<>();

    public MainFrame() {
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
        setResizable(true);
        setMinimumSize(new Dimension(600, 400)); // Prevents UI breaking
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainPanel.add(lp, LOGIN_PANEL);
        mainPanel.add(mp, MAIN_PANEL);
        add(mainPanel);

        showLoginPanel();
    }

    public void showLoginPanel() {
        setTitle("Login");
        cl.show(mainPanel, LOGIN_PANEL);
    }

    public void showMainPanel() {
        setTitle(title);
        cl.show(mainPanel, MAIN_PANEL);
    }

    public void addSocket(ClientSocket cs) {
        mp.addClient(cs);
        this.csList.add(cs);
        setListeners(cs);
    }

    public void closeSocket(ClientSocket cs) {
        try {
            cs.close();
        } catch (IOException e) {
            if (cs.isClosed()) return;
            System.err.println("Failed to close socket: " + e.getMessage());
        } finally {
            this.csList.remove(cs);

        }
    }

    private void setListeners(ClientSocket s) {
        s.setOnMessage(this::handleIncomingMessage);
        s.setOnMessageConfiguration(this::handleConfigMessage);
    }

    private void handleIncomingMessage(ClientSocket cs, Message msg) {
        mp.receiveMessage(cs, msg);
    }

    private void handleConfigMessage(ClientSocket cs, ConfigurationMessage confMessage) {
        mp.updateClient(cs);
    }

}
