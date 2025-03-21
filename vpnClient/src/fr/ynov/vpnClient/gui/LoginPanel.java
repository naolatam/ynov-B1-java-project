package fr.ynov.vpnClient.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;


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
        spPort = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
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
        String fqdn = txtHost.getText();
        if(fqdn.indexOf(".") == -1) {
             ErrorFrame.showError("Invalid FQDN/IP");
            return;
        }

        int port = Integer.parseInt(spPort.getValue().toString());
        btnConnect.setText(fqdn + ":" + port);
    }
}
