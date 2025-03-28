package fr.ynov.vpnModel.gui;

import javax.swing.JButton;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Font;

/**
 * A utility class that holds predefined color settings and a method to style components consistently across the application.
 * This class defines colors for various UI components and provides a method for styling buttons.
 */
public class StyleSet {

    // Predefined color settings for various UI components
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


    /**
     * Applies a consistent style to a given {@link JButton} by setting its font, colors, and border.
     *
     * @param button The {@link JButton} to be styled.
     * @see JButton
     */
    public static void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(StyleSet.buttonTextColor);
        button.setBackground(StyleSet.buttonBackgroundColor);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
    }

    /**
     * Private constructor to prevent instantiation of the utility class.
     * This constructor is not needed and has no effect, as the class is meant to be used statically.
     */
    private StyleSet() {}

}
