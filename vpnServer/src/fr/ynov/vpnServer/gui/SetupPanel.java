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

public class SetupPanel extends JPanel {

    private final MainFrame mf;
    private JTextField txtKey, txtName;
    private JSpinner spPort;
    private JButton btnStart;

    public SetupPanel(MainFrame parent) {
        this.mf = parent;
        setLayout(new GridBagLayout());
        setBackground(StyleSet.backgroundColor);

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

    private void addStartButton(GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        btnStart = new JButton("Start");
        styleButton(btnStart);
        btnStart.addActionListener(this::connectToServer);
        add(btnStart, gbc);
    }


    private void styleComponent(JComponent component) {
        component.setFont(new Font("Arial", Font.PLAIN, 14));
        component.setForeground(StyleSet.inputTextColor);
        component.setBackground(StyleSet.inputBackgroundColor);
        component.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    private void connectToServer(ActionEvent e) {
        btnStart.setText("Starting...");
        btnStart.setEnabled(false);

        int port = (int) spPort.getValue();
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    CustomServerSocket socket = new CustomServerSocket(port, txtName.getText());
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
