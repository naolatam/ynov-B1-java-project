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

    // All method beside do what their name mean they do.
    // Nothing more

    public void addSocket(ClientSocket cs) {
        mp.addClient(cs);
        this.csList.add(cs);
        setListeners(cs);
    }

    public void closeSocket(ClientSocket cs) {
        // Try to close the socket.
        // It also remove the socket from the clientlist if it is closed at the end
        try {
            cs.close();
        } catch (IOException e) {
            if (cs.isClosed()) return;
            System.err.println("Failed to close socket: " + e.getMessage());
        } finally {
            if(cs.isClosed()) {
                this.csList.remove(cs);
            }
        }
    }

    // Initialize the frame and content
    private void init() {
        // Set the frame title.
        setTitle(title);

        cl = new CardLayout();
        mainPanel = new JPanel(cl);

        // Define properties of the frame.
        setSize(800, 600);
        setResizable(true);
        setMinimumSize(new Dimension(600, 400)); // Prevents UI breaking
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainPanel.add(lp, LOGIN_PANEL);
        mainPanel.add(mp, MAIN_PANEL);
        add(mainPanel);

        // Show the login panel at first.
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

    // Set event listener of a ClientSocket
    private void setListeners(ClientSocket s) {
        s.setOnDisconnect(this::handleDisconnect);
        s.setOnMessage(this::handleIncomingMessage);
        s.setOnMessageConfiguration(this::handleConfigMessage);
    }

    // The method behind is the method called when a specifig event call.
    private Void handleDisconnect(ClientSocket cs) {
        mp.disconnectSocket(cs);
        return null;
    }

    private void handleIncomingMessage(ClientSocket cs, Message msg) {
        mp.receiveMessage(cs, msg);
    }

    private void handleConfigMessage(ClientSocket cs, ConfigurationMessage confMessage) {
        mp.updateClient(cs);
    }

}
