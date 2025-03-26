package fr.ynov.vpnModel.gui;

import javax.swing.*;
import java.awt.*;

public class Utils {

    public static JLabel createConfigMessageLabel(String text) {
        JLabel configLabel = new JLabel(text);
        configLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        configLabel.setForeground(StyleSet.labelTextColor);
        configLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return configLabel;
    }

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

}
