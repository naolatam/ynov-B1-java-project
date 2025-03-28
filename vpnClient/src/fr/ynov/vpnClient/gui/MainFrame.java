package fr.ynov.vpnClient.gui;

import fr.ynov.vpnClient.model.ClientSocket;
import fr.ynov.vpnModel.model.ConfigurationMessage;
import fr.ynov.vpnModel.model.Message;

import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.CardLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * MainFrame is the main window for the VPN Client application.
 * It manages different panels and handles client socket connections.
 */
public class MainFrame extends JFrame {
    private static final String LOGIN_PANEL = "login";
    private static final String MAIN_PANEL = "main";


    private final LoginPanel lp = new LoginPanel(this);
    private final MainPanel mp = new MainPanel(this);
    private final String title;
    private final List<ClientSocket> csList = new ArrayList<>();
    private CardLayout cl;
    private JPanel mainPanel;

    /**
     * Default constructor initializing the main frame with the default title.
     */
    public MainFrame() {
        super();
        this.title = "VPN Client";
        init();
    }

    /**
     * Constructor initializing the main frame with a custom title.
     *
     * @param title the title of the frame
     */
    public MainFrame(String title) {
        super(title);
        this.title = title;
        init();
    }

    // All method beside do what their name mean they do.
    // Nothing more

    /**
     * Adds a new client socket to the application and sets event listeners for it.
     *
     * @param cs the {@link fr.ynov.vpnClient.model.ClientSocket} to add
     */
    public void addSocket(ClientSocket cs) {
        mp.addClient(cs);
        this.csList.add(cs);
        setListeners(cs);
    }

    /**
     * Closes a client socket and removes it from the list if successfully closed.
     *
     * @param cs the client socket to close
     */
    public void closeSocket(ClientSocket cs) {
        // Try to close the socket.
        // It also remove the socket from the clientlist if it is closed at the end
        try {
            cs.close();
        } catch (IOException e) {
            if (cs.isClosed()) return;
            System.err.println("Failed to close socket: " + e.getMessage());
        } finally {
            if (cs.isClosed()) {
                this.csList.remove(cs);
            }
        }
    }

    /**
     * Initializes the frame, sets properties, and adds panels.
     */
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

    /**
     * Displays the login panel.
     */
    public void showLoginPanel() {
        setTitle("Login");
        cl.show(mainPanel, LOGIN_PANEL);
    }

    /**
     * Displays the main panel.
     */
    public void showMainPanel() {
        setTitle(title);
        cl.show(mainPanel, MAIN_PANEL);
    }

    /**
     * Sets event listeners for a client socket.
     *
     * @param cs the {@link fr.ynov.vpnClient.model.ClientSocket}
     */
    private void setListeners(ClientSocket cs) {
        cs.setOnDisconnect(this::handleDisconnect);
        cs.setOnMessage(this::handleIncomingMessage);
        cs.setOnMessageConfiguration(this::handleConfigMessage);
    }

    /**
     * Handles a client socket disconnection event.
     *
     * @param cs the client socket that disconnected
     * @return null (used for functional interface compatibility)
     */
    private Void handleDisconnect(ClientSocket cs) {
        mp.disconnectSocket(cs);
        return null;
    }

    /**
     * Handles an incoming message from a client socket.
     *
     * @param cs  the client socket that received the message
     * @param msg the received message
     */
    private void handleIncomingMessage(ClientSocket cs, Message msg) {
        mp.receiveMessage(cs, msg);
    }

    /**
     * Handles a configuration message from a client socket.
     *
     * @param cs          the client socket that received the configuration message
     * @param confMessage the configuration message
     */
    private void handleConfigMessage(ClientSocket cs, ConfigurationMessage confMessage) {
        mp.updateClient(cs);
    }

}
