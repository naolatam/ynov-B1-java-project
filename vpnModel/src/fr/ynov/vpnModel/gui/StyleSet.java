package fr.ynov.vpnModel.gui;

import javax.swing.*;
import java.awt.*;

public class StyleSet {
    public static Color backgroundColor = new Color(45, 52, 54);

    public static Color buttonBackgroundColor = new Color(41, 128, 185);
    public static Color buttonTextColor = new Color(220, 220, 220);

    public static Color inputTextColor = new Color(0, 0, 0);
    public static Color inputBackgroundColor = new Color(220, 220, 220);

    public static Color titleTextColor = new Color(255, 255, 255);
    public static Color titleBackgroundColor = new Color(220, 220, 220);

    public static Color labelTextColor = new Color(255, 255, 255);
    public static Color labelBackgroundColor = new Color(220, 220, 220);

    public static Color deleteButtonBackground = new Color(255, 0, 0);

    public static Color receivedMessageBackground = new Color(76, 175, 80);
    public static Color sendedMessageBackground = new Color(0, 123, 255);


    public static void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(StyleSet.buttonTextColor);
        button.setBackground(StyleSet.buttonBackgroundColor);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
    }

    public void StyleSet() {}

}
