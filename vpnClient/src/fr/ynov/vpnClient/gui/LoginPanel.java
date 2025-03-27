package fr.ynov.vpnClient.gui;

import fr.ynov.vpnClient.model.ClientSocket;
import fr.ynov.vpnClient.utils.Utils;
import fr.ynov.vpnModel.gui.ErrorFrame;
import fr.ynov.vpnModel.gui.StyleSet;
import fr.ynov.vpnModel.gui.SuccessFrame;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.ExecutionException;


public class LoginPanel extends JPanel {

    private final MainFrame mf;
    private final JTextField txtHost;
    private final JTextField txtKey;
    private final JTextField txtName;
    private final JSpinner spPort;
    private final JButton btnConnect;


    public LoginPanel(MainFrame parent) {
        this.mf = parent;
        // Set the layout and the background
        setLayout(new GridBagLayout());
        setBackground(StyleSet.backgroundColor); // Dark background

        // Set the grid constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        // Adding the title to the page
        addTitle(gbc);

        // Reset the grid Width and go to the next line
        gbc.gridwidth = 1;
        gbc.gridy++;

        // Creating and adding the form for login.
        JLabel lblHost = new JLabel("FQDN/IP:");
        lblHost.setForeground(StyleSet.labelTextColor);
        add(lblHost, gbc);

        gbc.gridx = 1;
        txtHost = new JTextField(15);
        styleTextField(txtHost);
        add(txtHost, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblPort = new JLabel("Port:");
        lblPort.setForeground(StyleSet.labelTextColor);
        add(lblPort, gbc);


        // Make a spinner for selecting the port on wich the server is.
        // Minimum value is, 1024 because all port before is used for system service like http, dns, dhcp, smtp, etc...
        // The maximum value is 49151 because it's the last port reserved for custom application.
        gbc.gridx = 1;
        spPort = new JSpinner(new SpinnerNumberModel(1024, 0, 49151, 1));
        spPort.setFont(new Font("Arial", Font.PLAIN, 14));
        spPort.setForeground(StyleSet.inputTextColor);
        spPort.setBackground(StyleSet.inputBackgroundColor);
        spPort.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(spPort, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblKey = new JLabel("AES Key:");
        lblKey.setForeground(StyleSet.labelTextColor);
        add(lblKey, gbc);

        gbc.gridx = 1;
        txtKey = new JTextField(15);
        styleTextField(txtKey);
        add(txtKey, gbc);
        txtKey.setEditable(false);
        // Generating and encoding in base64 a new key so it can be show to the user.
        txtKey.setText(Base64.getEncoder().encodeToString(generateKey().getEncoded()));

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblName = new JLabel("Username:");
        lblName.setForeground(StyleSet.labelTextColor);
        add(lblName, gbc);

        gbc.gridx = 1;
        txtName = new JTextField(15);
        styleTextField(txtName);
        add(txtName, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        btnConnect = new JButton("Connect");
        styleButton(btnConnect);
        add(btnConnect, gbc);

        btnConnect.addActionListener(this::connectToServer);

    }

    private void addTitle(GridBagConstraints gbc) {
        // Creating a new Label for title
        JLabel title = new JLabel("Connect to server");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(StyleSet.titleTextColor);
        add(title, gbc);
    }

    // This method is called when the connect button is clicked
    private void connectToServer(ActionEvent e) {
        // Change the label of the button and desactivate it
        setButtonState(false, "Connectiong...");

        // Fetching the data from the form
        String fqdn = txtHost.getText();
        int port = Integer.parseInt(spPort.getValue().toString());

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                // Check if the fqdn is valid
                if (!Utils.isValidFQDNorIP(fqdn)) {
                    ErrorFrame.showError("Invalid FQDN/IP");
                    return -2;
                }
                try {
                    // Creating a new socket and connect to the server.
                    ClientSocket socket = new ClientSocket(fqdn, port, txtName.getText());
                    // Parse the AES Key from the form and set it in the socket
                    socket.setPrivateKey(new SecretKeySpec(Base64.getDecoder().decode(txtKey.getText()), "AES"));
                    // Send the public Key of client to the server and ask for his public key
                    socket.askServerKey();
                    mf.addSocket(socket);
                    return 0; // Succès
                } catch (IOException ex) {
                    return -1; // Échec
                }
            }

            @Override
            protected void done() {
                try {
                    Integer success = get();

                    if (success == 0) {
                        SuccessFrame.showSuccess("Client socket connected: " + fqdn + ":" + port);
                        mf.showMainPanel();
                    } else if (success == -1) {
                        ErrorFrame.showError("Unable to connect to " + fqdn + ":" + port + "!");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    ErrorFrame.showError("An error occure: " + e.getMessage());

                }
                setButtonState(true, "Connect");

            }
        }.execute();

    }

    // This method is used to generate a AES Key
    private SecretKey generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");

            // Initialize the KeyGenerator with a key size (128, 192, or 256 bits)
            keyGen.init(256);  // AES supports key sizes: 128, 192, or 256 bits

            // Generate the AES secret key
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    // This method is used to change the state of the connect button
    private void setButtonState(boolean isEnabled, String text) {
        SwingUtilities.invokeLater(() -> {
            btnConnect.setText(text);
            btnConnect.setEnabled(isEnabled);
        });
    }

    // Styling the button
    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(StyleSet.buttonTextColor);
        button.setBackground(StyleSet.buttonBackgroundColor); // Blue
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);

    }

    // Styling the textField.
    private void styleTextField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setForeground(StyleSet.inputTextColor);
        field.setBackground(StyleSet.inputBackgroundColor);
        field.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

}
