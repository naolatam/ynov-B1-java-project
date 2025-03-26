package fr.ynov.vpnClient.gui;

import fr.ynov.vpnClient.model.ClientSocket;
import fr.ynov.vpnModel.gui.ErrorFrame;
import fr.ynov.vpnModel.gui.StyleSet;
import fr.ynov.vpnModel.model.ConfigurationMessage;
import fr.ynov.vpnModel.model.Message;
import fr.ynov.vpnModel.model.MessageType;
import fr.ynov.vpnModel.model.Origin;

import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class MainPanel extends JPanel {
    private final DefaultListModel<ClientSocket> clientListModel;
    private final JList<ClientSocket> clientList;
    private final JPanel chatArea;
    private final JTextField messageField;
    private final JLabel socketName;
    private final JButton sendButton;
    private final JButton closeButton;


    public MainPanel() {
        setLayout(new BorderLayout());

        // Liste des clients connectés
        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientList.addListSelectionListener(e -> loadConversation());
        clientList.setBackground(StyleSet.backgroundColor);
        clientList.setForeground(StyleSet.labelTextColor);
        JScrollPane listScroll = new JScrollPane(clientList);
        listScroll.setPreferredSize(new Dimension(200, 0));

        // Zone de chat
        chatArea = new JPanel();
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.Y_AXIS));
        chatArea.setBackground(StyleSet.backgroundColor);
        chatArea.setForeground(StyleSet.titleTextColor);
        JScrollPane chatScroll = new JScrollPane(chatArea);

        // Champ de saisie et bouton envoyer
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        sendButton = new JButton("Send");

        messageField.setBackground(StyleSet.backgroundColor);
        messageField.setForeground(StyleSet.labelTextColor);
        inputPanel.setBackground(StyleSet.backgroundColor);
        sendButton.setEnabled(false);
        styleButton(sendButton);
        sendButton.addActionListener(this::sendMessage);

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        JPanel infoPanel = new JPanel(new BorderLayout());
        socketName = new JLabel("No discuss");
        closeButton = new JButton("Close");
        infoPanel.setBackground(StyleSet.backgroundColor);
        socketName.setForeground(StyleSet.titleTextColor);
        closeButton.setEnabled(false);
        styleButton(closeButton);
        closeButton.addActionListener(this::closeSocket);

        infoPanel.add(socketName, BorderLayout.CENTER);
        infoPanel.add(closeButton, BorderLayout.EAST);


        // Séparateur entre la liste des clients et le chat
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, chatScroll);
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.3);

        add(splitPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        add(infoPanel, BorderLayout.NORTH);
    }

    private void loadConversation() {
        chatArea.removeAll();
        chatArea.revalidate();
        chatArea.repaint();
        ClientSocket selectedClient = clientList.getSelectedValue();
        if (selectedClient != null) {
            socketName.setText("Discuss: " + selectedClient.getServerName());
            closeButton.setEnabled(true);
            sendButton.setEnabled(true);
            if (selectedClient.isClosed()) {
                sendButton.setEnabled(false);
                closeButton.setEnabled(false);
            }

            selectedClient.getMessages().forEach(message -> {
                if (message.getType() == MessageType.CONFIG) {
                    switch (((ConfigurationMessage) message).getConfiguration()) {
                        case SET_NAME -> chatArea.add(
                                createConfigMessageLabel(
                                        message.getOrigin().name()
                                                + " send his name: "
                                                + message.getContent()
                                ));
                        case GET_PUBLIC_KEY, SEND_PUBLIC_KEY -> chatArea.add(
                                createConfigMessageLabel(
                                        message.getOrigin().name()
                                                + " send his key"
                                )
                        );
                    }
                    return;
                }
                boolean isSent = message.getOrigin() == Origin.SERVER;
                chatArea.add(createMessageLabel(message.getContent(), isSent));
                chatArea.revalidate();
                chatArea.repaint();
            });

        }
    }

    private void sendMessage(ActionEvent e) {
        ClientSocket selectedClient = clientList.getSelectedValue();
        if (selectedClient == null || selectedClient.isClosed()) {
            ErrorFrame.showError("Unable to send message. This socket is disconnected.");
            return;
        }
        if (!messageField.getText().trim().isEmpty()) {
            String message = messageField.getText().trim();

            try {
                selectedClient.sendMessage(message, true);

                chatArea.add(createMessageLabel(message, true));
                chatArea.revalidate();
                chatArea.repaint();

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Impossible d'envoyer le message", "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }

            messageField.setText("");
            messageField.grabFocus();
        }
    }

    private void closeSocket(ActionEvent e) {
        ClientSocket selectedClient = clientList.getSelectedValue();
        if (selectedClient == null) {
            ErrorFrame.showError("Unable to send message. This socket is undefined.");
            return;
        }
        try {
            selectedClient.sendMessage("Server close connection", false);
            selectedClient.close();
            loadConversation();
        } catch (IOException ex) {
            ErrorFrame.showError("Unable to send message. Error: " + ex.getMessage());
        } catch (NoSuchAlgorithmException ex) {
            // Cannot append because
        }
    }

    public void addClient(ClientSocket socket) {
        if (!clientListModel.contains(socket)) {
            clientListModel.addElement(socket);
        }
    }

    public void updateClient(ClientSocket socket) {
        int index = clientListModel.indexOf(socket);
        if (index != -1) {
            clientListModel.set(index, socket); // Force UI refresh
        }
    }

    public void receiveMessage(ClientSocket client, Message message) {
        if (message.isCrypted()) message.setContent("Unable to decrypt this message");
        if (clientList.getSelectedValue() == client) {
            chatArea.add(createMessageLabel(message.getContent(), false));
            chatArea.revalidate();
            chatArea.repaint();

        }
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(StyleSet.buttonTextColor);
        button.setBackground(StyleSet.buttonBackgroundColor);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
    }

    private JLabel createConfigMessageLabel(String text) {
        JLabel configLabel = new JLabel(text);
        configLabel.setFont(new Font("Arial", Font.ITALIC, 14)); // Italic font for distinction
        configLabel.setForeground(StyleSet.labelTextColor); // Uses your theme color
        configLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return configLabel;
    }

    private JLabel createMessageLabel(String text, boolean isSent) {
        JLabel messageLabel = new JLabel(text);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setOpaque(true);
        messageLabel.setForeground(Color.WHITE);

        if (isSent) {
            messageLabel.setBackground(new Color(0, 123, 255));
        } else {
            messageLabel.setBackground(new Color(230, 230, 230));
        }
        messageLabel.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 10)); // Padding for message bubble

        return messageLabel;
    }


}
