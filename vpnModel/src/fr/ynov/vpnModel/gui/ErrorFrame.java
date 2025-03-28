package fr.ynov.vpnModel.gui;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.BorderFactory;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Font;

/**
 * A custom modal dialog that displays an error message with an icon and an "OK" button.
 * This class extends {@link JDialog} to create an error popup window.
 * The window is modal, preventing interaction with other windows until closed.
 */
public class ErrorFrame extends JDialog {

    /**
     * Constructs an ErrorFrame to display the given error message.
     *
     * @param errorMessage The error message to be displayed in the dialog.
     */
    public ErrorFrame(String errorMessage) {

        // Setting Dialog properties
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

    /**
     * Styles the given button with specific fonts, colors, and borders.
     *
     * @param button The {@link JButton} to be styled.
     */
    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(StyleSet.buttonTextColor);
        button.setBackground(StyleSet.buttonBackgroundColor);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
    }

    /**
     * Displays an error message in a modal error dialog.
     * This method ensures that the error frame is created and displayed on the Event Dispatch Thread (EDT).
     *
     * @param message The error message to display in the dialog.
     */
    public static void showError(String message) {
        SwingUtilities.invokeLater(() -> new ErrorFrame(message).setVisible(true));
    }
}
