package fr.ynov.vpnServer.gui;

import fr.ynov.vpnModel.gui.StyleSet;
import fr.ynov.vpnServer.model.CustomSocket;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;


public class MainPanel extends JPanel {
        private DefaultListModel<CustomSocket> clientListModel;
        private JList<CustomSocket> clientList;
        private JTextArea chatArea;
        private JTextField messageField;
        private JButton sendButton;
        private HashMap<CustomSocket, StringBuilder> chatHistory;

        public MainPanel() {
            setLayout(new BorderLayout());

            // Liste des clients connectés
            clientListModel = new DefaultListModel<>();
            clientList = new JList<>(clientListModel);
            clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                clientList.addListSelectionListener(e -> loadConversation());

            JScrollPane listScroll = new JScrollPane(clientList);
            listScroll.setPreferredSize(new Dimension(200, 0));

            // Zone de chat
            chatArea = new JTextArea();
            chatArea.setEditable(false);
            chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
            chatArea.setBackground(StyleSet.backgroundColor);
            chatArea.setForeground(StyleSet.titleTextColor);
            JScrollPane chatScroll = new JScrollPane(chatArea);

            // Champ de saisie et bouton envoyer
            JPanel inputPanel = new JPanel(new BorderLayout());
            messageField = new JTextField();
            sendButton = new JButton("Envoyer");
            styleButton(sendButton);
            sendButton.addActionListener(this::sendMessage);

            inputPanel.add(messageField, BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);

            // Séparateur entre la liste des clients et le chat
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, chatScroll);
            splitPane.setDividerLocation(200);
            splitPane.setResizeWeight(0.3);

            add(splitPane, BorderLayout.CENTER);
            add(inputPanel, BorderLayout.SOUTH);

            chatHistory = new HashMap<>();
        }

        private void loadConversation() {
            CustomSocket selectedClient = clientList.getSelectedValue();
            if (selectedClient != null) {
                chatArea.setText(chatHistory.getOrDefault(selectedClient, new StringBuilder()).toString());
            }
        }

        private void sendMessage(ActionEvent e) {
            CustomSocket selectedClient = clientList.getSelectedValue();
            if (selectedClient != null && !messageField.getText().trim().isEmpty()) {
                String message = "Serveur: " + messageField.getText().trim();

                // Ajout dans l'historique local
                chatHistory.putIfAbsent(selectedClient, new StringBuilder());
                chatHistory.get(selectedClient).append(message).append("\n");

                // Affichage dans l'interface
                chatArea.append(message + "\n");

                // Envoi du message au client
                try {
                    PrintWriter out = new PrintWriter(selectedClient.getSocket().getOutputStream(), true);
                    out.println(message);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Impossible d'envoyer le message", "Erreur", JOptionPane.ERROR_MESSAGE);
                }

                messageField.setText("");
            }
        }

        public void addClient(CustomSocket socket) {
            if (!clientListModel.contains(socket)) {
                clientListModel.addElement(socket);
                chatHistory.put(socket, new StringBuilder());
            }
        }

        public void updateClient(CustomSocket socket) {
            int index = clientListModel.indexOf(socket);
            if (index != -1) {
                clientListModel.set(index, socket); // Force UI refresh
            }
        }

        public void receiveMessage(CustomSocket client, String message) {
            chatHistory.putIfAbsent(client, new StringBuilder());
            chatHistory.get(client).append("Client: ").append(message).append("\n");

            if (clientList.getSelectedValue() == client) {
                chatArea.append("Client: " + message + "\n");
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
