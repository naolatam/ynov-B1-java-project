package fr.ynov.vpnServer.gui;

import fr.ynov.vpnModel.gui.ErrorFrame;
import fr.ynov.vpnModel.gui.StyleSet;
import fr.ynov.vpnModel.gui.SuccessFrame;
import fr.ynov.vpnServer.model.CustomServerSocket;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static fr.ynov.vpnModel.gui.StyleSet.styleButton;

/**
 * SetupPanel is a JPanel representing the setup interface for configuring and starting the VPN server.
 * It allows the user to specify a port, generate an AES key, and enter a server name.
 */
public class SetupPanel extends JPanel {

    private final MainFrame mf;
    private JTextField txtKey, txtName;
    private JSpinner spPort;
    private JButton btnStart;

    /**
     * Constructs the SetupPanel with the given MainFrame as its parent.
     *
     * @param parent The main application frame that contains this panel.
     */
    public SetupPanel(MainFrame parent) {
        this.mf = parent;
        setLayout(new GridBagLayout());
        setBackground(StyleSet.backgroundColor);

        // Initialing the grid
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        addTitle(gbc);
        addPortInput(gbc);
        addKeyInput(gbc);
        addNameInput(gbc);
        addStartButton(gbc);
    }

    /**
     * Adds a title label to the panel.
     *
     * @param gbc {@link java.awt.GridBagConstraints} object for positioning.
     */
    private void addTitle(GridBagConstraints gbc) {
        JLabel title = new JLabel("Server setup");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(StyleSet.titleTextColor);
        add(title, gbc);

        gbc.gridy++;
        JLabel subTitle = new JLabel("On which port do you want to run the server socket?");
        subTitle.setFont(new Font("Arial", Font.BOLD, 16));
        subTitle.setForeground(StyleSet.titleTextColor);
        add(subTitle, gbc);
    }

    /**
     * Add the port input into the form.
     *
     * @param gbc {@link java.awt.GridBagConstraints} object for positioning.
     */
    private void addPortInput(GridBagConstraints gbc) {
        gbc.gridwidth = 1;
        gbc.gridy++;
        JLabel lblPort = new JLabel("Port:");
        lblPort.setForeground(StyleSet.labelTextColor);
        add(lblPort, gbc);

        gbc.gridx = 1;
        spPort = new JSpinner(new SpinnerNumberModel(1024, 0, 49151, 1));
        styleComponent(spPort);
        add(spPort, gbc);
    }

    /**
     * Adds the AES key input zone.
     *
     * @param gbc {@link java.awt.GridBagConstraints} object for positioning.
     */
    private void addKeyInput(GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblKey = new JLabel("AES Key:");
        lblKey.setForeground(StyleSet.labelTextColor);
        add(lblKey, gbc);

        gbc.gridx = 1;
        txtKey = new JTextField(15);
        styleComponent(txtKey);
        txtKey.setEditable(false);
        txtKey.setText(Base64.getEncoder().encodeToString(generateKey().getEncoded()));
        add(txtKey, gbc);
    }

    /**
     * Adds a name input to the panel.
     *
     * @param gbc {@link java.awt.GridBagConstraints} object for positioning.
     */
    private void addNameInput(GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblName = new JLabel("Server name:");
        lblName.setForeground(StyleSet.labelTextColor);
        add(lblName, gbc);

        gbc.gridx = 1;
        txtName = new JTextField(15);
        styleComponent(txtName);
        add(txtName, gbc);
    }

    /**
     * Adds the start button to the panel.
     *
     * @param gbc {@link java.awt.GridBagConstraints} object for positioning.
     */
    private void addStartButton(GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        btnStart = new JButton("Start");
        styleButton(btnStart);
        btnStart.addActionListener(this::startTheServer);
        add(btnStart, gbc);
    }

    /**
     * Stylize a component
     *
     * @param component {@link javax.swing.JComponent} component to stylize.
     */
    private void styleComponent(JComponent component) {
        component.setFont(new Font("Arial", Font.PLAIN, 14));
        component.setForeground(StyleSet.inputTextColor);
        component.setBackground(StyleSet.inputBackgroundColor);
        component.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    /**
     * Handles the starting process when the start button is clicked.
     *
     * @param e The ActionEvent triggered by the button click.
     */
    private void startTheServer(ActionEvent e) {
        btnStart.setText("Starting...");
        btnStart.setEnabled(false);

        int port = (int) spPort.getValue();
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                // Trying to start the server socket
                try {
                    CustomServerSocket socket = new CustomServerSocket(port, txtName.getText());
                    // setting the privateKey if it's start well!
                    socket.setPrivateKey(new SecretKeySpec(Base64.getDecoder().decode(txtKey.getText()), "AES"));
                    mf.setServerSocket(socket);
                    return true;
                } catch (IOException ex) {
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    // If connexion return true
                    if (get()) {
                        SuccessFrame.showSuccess("Server socket started on port: " + port);
                        mf.showMainPanel();
                    } else {
                        ErrorFrame.showError("Unable to connect using port: " + port + "!");
                    }
                } catch (Exception ex) {
                    ErrorFrame.showError("Unexpected error: " + ex.getMessage());
                }
                btnStart.setText("Start");
                btnStart.setEnabled(true);
            }
        }.execute();
    }

    /**
     * Generates a new AES key for secure communication.
     *
     * @return A newly generated AES {@link SecretKey}.
     */
    private SecretKey generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Error generating AES key: " + ex.getMessage(), ex);
        }
    }
}
