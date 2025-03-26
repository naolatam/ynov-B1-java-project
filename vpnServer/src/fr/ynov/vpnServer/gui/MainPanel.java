package fr.ynov.vpnServer.gui;

import fr.ynov.vpnModel.gui.ErrorFrame;
import fr.ynov.vpnModel.gui.StyleSet;
<<<<<<< Updated upstream
=======
import fr.ynov.vpnModel.model.*;
import fr.ynov.vpnServer.model.CustomSocket;
>>>>>>> Stashed changes

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
<<<<<<< Updated upstream
import java.awt.event.ActionListener;
import java.net.ServerSocket;
=======
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
    private void sendMessage() {
        String selectedChat = conversationList.getSelectedValue();
        if (selectedChat != null) {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                String chatText = chatHistory.getOrDefault(selectedChat, "");
                chatText += "Moi: " + message + "\n";
                chatHistory.put(selectedChat, chatText);
                chatArea.setText(chatText);
=======
        private void loadConversation() {
            CustomSocket selectedClient = clientList.getSelectedValue();
            if (selectedClient != null) {
                sendButton.setEnabled(true);
                if(selectedClient.getSocket().isClosed()) {
                    sendButton.setEnabled(false);
                }
                ArrayList<String> content = new ArrayList<>();
                selectedClient.getMessages().forEach(message -> {
                    if(message.getType() == MessageType.CONFIG) {
                        if(((ConfigurationMessage) message).getConfiguration() == SocketConfiguration.SET_NAME) {
                            content.add(message.getOrigin().name() + " send his name: "  + message.getContent());
                            return;
                        }
                    }
                    content.add(message.getContent());
                });

                chatArea.setText(content.toString());
            }
        }

        private void sendMessage(ActionEvent e) {
            CustomSocket selectedClient = clientList.getSelectedValue();
            if(selectedClient == null || selectedClient.getSocket().isClosed()) {
                ErrorFrame.showError("Unable to send message. This socket is disconnected.");
                return;
            }
            if (!messageField.getText().trim().isEmpty()) {
                String message = selectedClient.getName().substring(0, 20) +  ": " + messageField.getText().trim();

                try {
                    selectedClient.sendMessage(message, true);
                    chatHistory.putIfAbsent(selectedClient, new StringBuilder());
                    chatHistory.get(selectedClient).append(message).append("\n");

                    chatArea.append(message + "\n");

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Impossible d'envoyer le message", "Erreur", JOptionPane.ERROR_MESSAGE);
                } catch (NoSuchAlgorithmException ex) {
                    throw new RuntimeException(ex);
                }

>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(StyleSet.buttonTextColor);
        button.setBackground(StyleSet.buttonBackgroundColor);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
    }
=======
        public void updateClient(CustomSocket socket) {
            int index = clientListModel.indexOf(socket);
            if (index != -1) {
                clientListModel.set(index, socket); // Force UI refresh
            }
        }

        public void receiveMessage(CustomSocket client, Message message) {
            if(message.isCrypted()) message.setContent("Unable to decrypt this message");
            chatHistory.putIfAbsent(client, new StringBuilder());
            chatHistory.get(client).append(message.getContent()).append("\n");

            if (clientList.getSelectedValue() == client) {
                chatArea.append(message.getContent() + "\n");
            }
        }

        private void styleButton(JButton button) {
            button.setFont(new Font("Arial", Font.BOLD, 14));
            button.setForeground(StyleSet.buttonTextColor);
            button.setBackground(StyleSet.buttonBackgroundColor);
            button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            button.setFocusPainted(false);
        }



>>>>>>> Stashed changes
}
