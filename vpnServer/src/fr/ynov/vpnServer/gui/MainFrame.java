package fr.ynov.vpnServer.gui;

import fr.ynov.vpnModel.model.ConfigurationMessage;
import fr.ynov.vpnModel.model.Message;
import fr.ynov.vpnServer.model.CustomServerSocket;
import fr.ynov.vpnServer.model.CustomSocket;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;

public class MainFrame  extends JFrame {
    private CustomServerSocket ss = null;
    private final SetupPanel sp = new SetupPanel(this);
    private final MainPanel mp = new MainPanel();

    private final String title;
    private CardLayout cl;
    private JPanel mainPanel;

    public MainFrame() {
        super();
        this.title = "Server app";
        setTitle(title);
        init();
    }
    public MainFrame(String title) {
        super(title);
        this.title = title;
        init();
    }

    private void init() {
        setTitle(title);

        getContentPane().removeAll();
        cl = new CardLayout();
        mainPanel = new JPanel(cl);

        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if(ss == null) {
            showSetupPanel();
        }else {
            showMainPanel();
        }

        mainPanel.add(sp, "sp");
        mainPanel.add(mp, "main");

        add(mainPanel);
    }

    private void showSetupPanel() {
        cl.show(mainPanel, "sp");
    }
    public void showMainPanel() {
        cl.show(mainPanel, "main");


    }

    public void setServerSocket(CustomServerSocket ss) throws IOException {
        if(this.ss != null && ss.isClosed()) {
            this.ss.close();
        }
        this.ss = ss;
        setListener();
    }

    private void setListener() {
        this.ss.setOnConnect((CustomSocket cs) -> {
            if(cs.getName() == "" || cs.getName() == null) {
                mp.addConversation(cs.getUuid().toString());
                return null;
            }
            mp.addConversation(cs.getName() + " (" + cs.getUuid().toString() + ")");
            return null;
        });

        this.ss.setOnMessage((CustomSocket cs, Message msg) -> {
            System.out.println("From: " + cs.toString() + ", Message: " + msg.getContent());
            mp.receiveMessage(cs, msg);
        });

        this.ss.setOnMessageConfiguration((CustomSocket cs, ConfigurationMessage confMessage) -> {
            System.out.println("From: " + cs.toString() + ", Message: " + confMessage.getContent());
            return;
        });
    }

}
