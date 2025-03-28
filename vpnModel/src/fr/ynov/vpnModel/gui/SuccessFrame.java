package fr.ynov.vpnModel.gui;

import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Font;

import static fr.ynov.vpnModel.gui.StyleSet.styleButton;

/**
 * A custom modal dialog that displays a success message with an icon and an "OK" button.
 * This class extends {@link JDialog} to create a popup window indicating success.
 * The window is modal, meaning it prevents interaction with other windows until closed.
 */
public class SuccessFrame extends JDialog {

    /**
     * Constructs a SuccessFrame to display the given success message.
     *
     * @param successMessage The success message to be displayed in the dialog.
     */
    public SuccessFrame(String successMessage) {
        // Set modal properties
        setTitle("Success");
        setSize(350, 180);
        setLocationRelativeTo(null);
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(StyleSet.backgroundColor);

        // Initialize grid
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        // Title Label
        JLabel lblTitle = new JLabel("Success");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setForeground(StyleSet.titleTextColor);
        add(lblTitle, gbc);

        // Error Icon
        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel lblIcon = new JLabel(UIManager.getIcon("OptionPane.informationIcon"));
        add(lblIcon, gbc);

        // Error Message
        gbc.gridx = 1;
        JLabel lblMessage = new JLabel("<html><body style='width: 180px;'>" + successMessage + "</body></html>");
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
     * Displays a success message in a modal success dialog.
     * This method ensures that the success frame is created and displayed on the Event Dispatch Thread (EDT).
     *
     * @param message The success message to display in the dialog.
     */

    public static void showSuccess(String message) {
        SwingUtilities.invokeLater(() -> new SuccessFrame(message).setVisible(true));
    }
}
