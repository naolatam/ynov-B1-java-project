package fr.ynov.vpnModel.gui;

import javax.swing.*;
import java.awt.*;

/**
 * A utility class containing helper methods for creating commonly used UI components and performing common tasks.
 * The methods in this class help streamline the creation of message labels and add functionality like sleeping for a specified time.
 */
public class Utils {

    /**
     * Creates and styles a JLabel for displaying a configuration message.
     * The label is styled with italic font and the predefined label text color.
     *
     * @param text The text to be displayed on the label.
     * @return A styled {@link JLabel} for the configuration message.
     */
    public static JLabel createConfigMessageLabel(String text) {
        JLabel configLabel = new JLabel(text);
        configLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        configLabel.setForeground(StyleSet.labelTextColor);
        configLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return configLabel;
    }

    /**
     * Creates and styles a JLabel for displaying a message.
     * The background color of the label depends on whether the message was sent or received.
     *
     * @param text The text to be displayed on the label.
     * @param isSent A boolean indicating whether the message was sent (true) or received (false).
     * @return A styled {@link JLabel} for the message.
     */
    public static JLabel createMessageLabel(String text, boolean isSent) {
        JLabel messageLabel = new JLabel(text);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setOpaque(true);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setBackground(
                isSent ?
                        StyleSet.sendedMessageBackground :
                        StyleSet.receivedMessageBackground);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 10));

        return messageLabel;
    }
    
    /**
     * Pauses the current thread for a specified amount of time (in milliseconds).
     * If the thread is interrupted during sleep, it will reset the interrupt flag.
     *
     * @param milis The time to sleep in milliseconds.
     */
    public static void sleep(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
