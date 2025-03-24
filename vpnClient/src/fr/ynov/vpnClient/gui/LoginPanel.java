package fr.ynov.vpnClient.gui;

import fr.ynov.vpnClient.model.ClientSocket;
import fr.ynov.vpnClient.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;


public class LoginPanel extends JPanel {

    private JTextField txtHost;
    private JSpinner spPort;
    private JButton btnConnect;
    private final MainFrame mf;


    public LoginPanel(MainFrame parent) {
        this.mf = parent;
        setLayout(new GridBagLayout());
        setBackground(StyleSet.backgroundColor); // Dark background

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel title = new JLabel("Server Login");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(StyleSet.titleTextColor);
        add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
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

        gbc.gridx = 1;
        spPort = new JSpinner(new SpinnerNumberModel(1024, 0, 49151, 1));
        spPort.setFont(new Font("Arial", Font.PLAIN, 14));
        spPort.setForeground(StyleSet.inputTextColor);
        spPort.setBackground(StyleSet.inputBackgroundColor);
        spPort.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(spPort, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        btnConnect = new JButton("Connect");
        styleButton(btnConnect);
        add(btnConnect, gbc);

        btnConnect.addActionListener(this::connectToServer);

    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setForeground(StyleSet.inputTextColor);
        field.setBackground(StyleSet.inputBackgroundColor);
        field.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(StyleSet.buttonTextColor);
        button.setBackground(StyleSet.buttonBackgroundColor); // Blue
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
    }

    private void connectToServer(ActionEvent e) {
        SwingUtilities.invokeLater(() ->  btnConnect.setText("Connecting..."));
        SwingUtilities.invokeLater(() ->  btnConnect.setEnabled(false));

        String fqdn = txtHost.getText();
        int port = Integer.parseInt(spPort.getValue().toString());

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                if(!Utils.isValidFQDNorIP(fqdn)) {
                    ErrorFrame.showError("Invalid FQDN/IP");
                    return -2;
                }
                try {
                    ClientSocket socket = new ClientSocket(fqdn, port );
                    mf.addSocket(socket);
                    return 0; // Succès
                } catch (IOException | InterruptedException ex) {
                    return -1; // Échec
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            protected void done() {
                try {
                    Integer success = get();
                    if (success==0) {
                        JOptionPane.showMessageDialog(LoginPanel.this, "Connexion réussi !", "Erreur", JOptionPane.ERROR_MESSAGE);

                    } else if(success==-1) {
                        ErrorFrame.showError("Unable to connect to " + fqdn + ":" + port + "!");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LoginPanel.this, "Erreur inattendue : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
                SwingUtilities.invokeLater(() ->  btnConnect.setText("Connect"));
                SwingUtilities.invokeLater(() ->          btnConnect.setEnabled(true));
            }
        }.execute();

    }
}
