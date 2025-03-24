package fr.ynov.vpnModel.gui;

import javax.swing.*;
import java.awt.*;

public class ErrorFrame extends JDialog {
    public ErrorFrame(String errorMessage) {
        setTitle("Error");
        setSize(350, 180);
        setLocationRelativeTo(null);
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(StyleSet.backgroundColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        // Title Label
        JLabel lblTitle = new JLabel("Error");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setForeground(StyleSet.titleTextColor);
        add(lblTitle, gbc);

        // Error Icon
        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel lblIcon = new JLabel(UIManager.getIcon("OptionPane.errorIcon"));
        add(lblIcon, gbc);

        // Error Message
        gbc.gridx = 1;
        JLabel lblMessage = new JLabel("<html><body style='width: 180px;'>" + errorMessage + "</body></html>");
        lblMessage.setFont(new Font("Arial", Font.PLAIN, 14));
        lblMessage.setForeground(StyleSet.labelTextColor);
        add(lblMessage, gbc);

        // "OK" Button
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JButton btnClose = new JButton("OK");
        styleButton(btnClose);
        add(btnClose, gbc);

        btnClose.addActionListener(e -> dispose());
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(StyleSet.buttonTextColor);
        button.setBackground(StyleSet.buttonBackgroundColor);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
    }

    public static void showError(String message) {
        SwingUtilities.invokeLater(() -> new ErrorFrame(message).setVisible(true));
    }
}
