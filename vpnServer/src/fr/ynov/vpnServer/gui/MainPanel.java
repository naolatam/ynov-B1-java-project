package fr.ynov.vpnServer.gui;

import fr.ynov.vpnModel.gui.StyleSet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.ServerSocket;
import java.util.HashMap;

public class MainPanel extends JPanel {
    private JList<String> conversationList;
    private DefaultListModel<String> listModel;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private HashMap<String, String> chatHistory; // Stocke les discussions

    public MainPanel() {
        setLayout(new BorderLayout());

        // Liste des conversations à gauche
        listModel = new DefaultListModel<>();
        conversationList = new JList<>(listModel);
        conversationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        conversationList.setBackground(StyleSet.inputBackgroundColor);
        conversationList.setForeground(StyleSet.inputTextColor);
        conversationList.setFont(new Font("Arial", Font.PLAIN, 14));
        conversationList.setSelectionBackground(StyleSet.buttonBackgroundColor);
        conversationList.setSelectionForeground(Color.WHITE);
        conversationList.addListSelectionListener(e -> loadConversation());

        JScrollPane listScroll = new JScrollPane(conversationList);
        listScroll.setPreferredSize(new Dimension(200, 0));

        // Zone de discussion à droite
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        chatArea.setBackground(StyleSet.backgroundColor);
        chatArea.setForeground(StyleSet.titleTextColor);
        JScrollPane chatScroll = new JScrollPane(chatArea);

        // Barre d'envoi en bas
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));
        messageField.setBackground(StyleSet.inputBackgroundColor);
        messageField.setForeground(StyleSet.inputTextColor);
        sendButton = new JButton("Envoyer");
        styleButton(sendButton);
        sendButton.addActionListener(e -> sendMessage());

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Séparateur gauche/droite
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, chatScroll);
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.3);

        add(splitPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        chatHistory = new HashMap<>();
    }

    private void loadConversation() {
        String selectedChat = conversationList.getSelectedValue();
        if (selectedChat != null) {
            chatArea.setText(chatHistory.getOrDefault(selectedChat, ""));
        }
    }

    private void sendMessage() {
        String selectedChat = conversationList.getSelectedValue();
        if (selectedChat != null) {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                String chatText = chatHistory.getOrDefault(selectedChat, "");
                chatText += "Moi: " + message + "\n";
                chatHistory.put(selectedChat, chatText);
                chatArea.setText(chatText);
                messageField.setText("");
            }
        }
    }

    public void addConversation(String name) {
        if (!listModel.contains(name)) {
            listModel.addElement(name);
            chatHistory.put(name, "");
        }
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(StyleSet.buttonTextColor);
        button.setBackground(StyleSet.buttonBackgroundColor);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
    }
}
