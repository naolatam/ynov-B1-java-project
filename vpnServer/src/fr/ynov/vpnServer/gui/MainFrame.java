package fr.ynov.vpnServer.gui;

import fr.ynov.vpnModel.model.ConfigurationMessage;
import fr.ynov.vpnModel.model.Message;
import fr.ynov.vpnServer.model.CustomServerSocket;
import fr.ynov.vpnServer.model.CustomSocket;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MainFrame extends JFrame {

    private final String SETUP_PANEL = "setup";
    private final String MAIN_PANEL = "main";
    private final SetupPanel sp = new SetupPanel(this);
    private final MainPanel mp = new MainPanel();
    private final String title;
    private CustomServerSocket ss = null;
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

        mainPanel.add(sp, SETUP_PANEL);
        mainPanel.add(mp, MAIN_PANEL);

        add(mainPanel);

        showSetupPanel();
    }

    private void showSetupPanel() {
        cl.show(mainPanel, SETUP_PANEL);
    }

    public void showMainPanel() {
        cl.show(mainPanel, MAIN_PANEL);
    }

    public void setServerSocket(CustomServerSocket ss) throws IOException {
        if (this.ss != null && !this.ss.isClosed()) {
            this.ss.close();
        }
        this.ss = ss;
        setListener();
    }


    private void setListener() {
        this.ss.setOnConnect(this::handleConnection);
        this.ss.setOnDisconnect(this::handleDisconnect);
        this.ss.setOnMessage(this::handleIncomingMessage);
        this.ss.setOnMessageConfiguration(this::handleConfigurationMessage);
    }

    private Void handleConnection(CustomSocket cs) {
        mp.addClient(cs);
        return null;
    }

    private Void handleDisconnect(CustomSocket cs) {
        mp.disconnectSocket(cs);
        return null;
    }

    private void handleIncomingMessage(CustomSocket cs, Message msg) {
        mp.receiveMessage(cs, msg);
    }

    private void handleConfigurationMessage(CustomSocket cs, ConfigurationMessage confMessage) {
        mp.updateClient(cs);
    }

}
