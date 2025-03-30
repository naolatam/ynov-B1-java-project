package fr.ynov.vpnServer.gui;

import fr.ynov.vpnModel.model.ConfigurationMessage;
import fr.ynov.vpnModel.model.Message;
import fr.ynov.vpnServer.model.CustomServerSocket;
import fr.ynov.vpnServer.model.CustomSocket;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.io.IOException;

/**
 * MainFrame is the main graphical user interface for the VPN server application.
 * It manages the transition between different panels and handles server socket events.
 */
public class MainFrame extends JFrame {

    private final String SETUP_PANEL = "setup";
    private final String MAIN_PANEL = "main";
    private final SetupPanel sp = new SetupPanel(this);
    private final MainPanel mp = new MainPanel();
    private final String title;
    private CustomServerSocket ss = null;
    private CardLayout cl;
    private JPanel mainPanel;

    /**
     * Default constructor initializing the frame with default title.
     */
    public MainFrame() {
        super();
        this.title = "Server app";
        setTitle(title);
        init();
    }


    /**
     * Constructor initializing the frame with custom title.
     *
     * @param title The title of the window
     */
    public MainFrame(String title) {
        super(title);
        this.title = title;
        init();
    }

    /**
     * Initializes the frame by setting up components and layout.
     */
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

    /**
     * Displays the setup panel.
     */
    private void showSetupPanel() {
        cl.show(mainPanel, SETUP_PANEL);
    }

    /**
     * Displays the main panel.
     */
    public void showMainPanel() {
        cl.show(mainPanel, MAIN_PANEL);
    }

    /**
     * Sets the server socket and initializes its event listeners.
     *
     * @param ss The {@link CustomServerSocket} to be used
     * @throws IOException If an I/O error occurs while closing an existing socket
     */
    public void setServerSocket(CustomServerSocket ss) throws IOException {
        if (this.ss != null && !this.ss.isClosed()) {
            this.ss.close();
        }
        this.ss = ss;
        setListener();
    }

    /**
     * Sets up event listeners for the server socket.
     */
    private void setListener() {
        this.ss.setOnConnect(this::handleConnection);
        this.ss.setOnDisconnect(this::handleDisconnect);
        this.ss.setOnMessage(this::handleIncomingMessage);
        this.ss.setOnMessageConfiguration(this::handleConfigurationMessage);
    }

    /**
     * Handles a new client connection event.
     *
     * @param cs The connected {@link CustomSocket}
     * @return Always returns null
     */
    private Void handleConnection(CustomSocket cs) {
        mp.addClient(cs);
        return null;
    }

    /**
     * Handles client disconnection event.
     *
     * @param cs The disconnected {@link CustomSocket}
     * @return Always returns null
     */
    private Void handleDisconnect(CustomSocket cs) {
        mp.disconnectSocket(cs);
        return null;
    }


    /**
     * Handles an incoming message from a client.
     *
     * @param cs  The {@link CustomSocket} sending the message
     * @param msg The received {@link Message}
     */
    private void handleIncomingMessage(CustomSocket cs, Message msg) {
        mp.receiveMessage(cs, msg);
    }


    /**
     * Handles a Configuration incoming message from a client.
     *
     * @param cs  The {@link CustomSocket} sending the message
     * @param confMessage The received {@link ConfigurationMessage}
     */
    private void handleConfigurationMessage(CustomSocket cs, ConfigurationMessage confMessage) {
        mp.updateClient(cs);
    }

}
