package fr.ynov.vpnClient.gui;

import fr.ynov.vpnClient.model.ClientSocket;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private final LoginPanel lp = new LoginPanel(this);
    private CardLayout cl;
    private JPanel mainPanel;

    private List<ClientSocket> csList = new ArrayList<>();
    public MainFrame() {
        super();
        init();
    }
    public MainFrame(String title) {
        super(title);
        init();
    }

    private void init() {

        getContentPane().removeAll();
        cl = new CardLayout();
        mainPanel = new JPanel(cl);

        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if(csList.isEmpty()) {
            showLoginPanel();
        }
        add(mainPanel);
    }

    public void showLoginPanel() {
        setTitle("Login");
        mainPanel.add(lp, "Login");
    }


}
