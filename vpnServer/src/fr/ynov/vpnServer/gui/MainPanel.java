package fr.ynov.vpnServer.gui;

import fr.ynov.vpnModel.gui.ErrorFrame;
import fr.ynov.vpnModel.gui.StyleSet;
import fr.ynov.vpnModel.gui.Utils;
import fr.ynov.vpnModel.model.ConfigurationMessage;
import fr.ynov.vpnModel.model.Message;
import fr.ynov.vpnModel.model.MessageType;
import fr.ynov.vpnModel.model.Origin;
import fr.ynov.vpnServer.model.CustomSocket;

import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class MainPanel extends JPanel {
    private final DefaultListModel<CustomSocket> clientListModel;
    private final JList<CustomSocket> clientList;
    private final JPanel chatArea;
    private final JTextField messageField;
    private final JLabel socketName;
    private final JButton sendButton, closeButton, deleteButton;


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
        StyleSet.styleButton(sendButton);
        sendButton.addActionListener(this::sendMessage);

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        JPanel infoPanel = new JPanel(new BorderLayout());
        socketName = new JLabel("No discuss");
        closeButton = new JButton("Close");
        deleteButton = new JButton("Delete");

        infoPanel.setBackground(StyleSet.backgroundColor);
        socketName.setForeground(StyleSet.titleTextColor);
        closeButton.setEnabled(false);
        deleteButton.setEnabled(false);
        StyleSet.styleButton(closeButton);
        StyleSet.styleButton(deleteButton);
        deleteButton.setBackground(StyleSet.deleteButtonBackground);
        closeButton.addActionListener(this::closeSocket);
        deleteButton.addActionListener(this::deleteSocket);

        infoPanel.add(socketName, BorderLayout.CENTER);
        infoPanel.add(closeButton, BorderLayout.EAST);
        infoPanel.add(deleteButton, BorderLayout.WEST);


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
        CustomSocket selectedClient = clientList.getSelectedValue();
        if (selectedClient != null) {
            socketName.setText("Discuss: " + selectedClient.getName());
            closeButton.setEnabled(true);
            sendButton.setEnabled(true);
            deleteButton.setEnabled(false);
            if (selectedClient.getSocket().isClosed()) {
                deleteButton.setEnabled(true);
                sendButton.setEnabled(false);
                closeButton.setEnabled(false);
            }

            selectedClient.getMessages().forEach(message -> {
                if (message.getType() == MessageType.CONFIG) {
                    switch (((ConfigurationMessage) message).getConfiguration()) {
                        case SET_NAME -> chatArea.add(
                                Utils.createConfigMessageLabel(
                                        message.getOrigin().name()
                                                + " send his name: "
                                                + message.getContent()
                                ));
                        case GET_PUBLIC_KEY, SEND_PUBLIC_KEY -> chatArea.add(
                                Utils.createConfigMessageLabel(
                                        message.getOrigin().name()
                                                + " send his key"
                                )
                        );
                    }
                    return;
                }
                boolean isSent = message.getOrigin() == Origin.SERVER;
                chatArea.add(Utils.createMessageLabel(message.getContent(), isSent));
                chatArea.revalidate();
                chatArea.repaint();
            });

        }else {
            deleteButton.setEnabled(false);
            sendButton.setEnabled(false);
            closeButton.setEnabled(false);

        }
    }

    private void sendMessage(ActionEvent e) {
        CustomSocket selectedClient = clientList.getSelectedValue();
        if (selectedClient == null || selectedClient.getSocket().isClosed()) {
            ErrorFrame.showError("Unable to send message. This socket is disconnected.");
            return;
        }
        if (!messageField.getText().trim().isEmpty()) {
            String message = messageField.getText().trim();

            try {
                selectedClient.sendMessage(message, true);

                chatArea.add(Utils.createMessageLabel(message, true));
                chatArea.revalidate();
                chatArea.repaint();

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Impossible d'envoyer le message", "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
                throw new RuntimeException(ex);
            }

            messageField.setText("");
            messageField.grabFocus();
        }
    }

    private void closeSocket(ActionEvent e) {
        CustomSocket selectedClient = clientList.getSelectedValue();
        if (selectedClient == null) {
            ErrorFrame.showError("Unable to send message. This socket is undefined.");
            return;
        }
        try {
            selectedClient.sendMessage("Server close connection", false);
            selectedClient.getSocket().close();
            loadConversation();
        } catch (IOException ex) {
            ErrorFrame.showError("Unable to send message. Error: " + ex.getMessage());
        } catch (NoSuchPaddingException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            // Cannot append because
        }
    }
    private void deleteSocket(ActionEvent e) {
        CustomSocket selectedClient = clientList.getSelectedValue();
        if (selectedClient == null) {
            ErrorFrame.showError("Unable to delete this. This socket is undefined.");
            return;
        }
        clientListModel.removeElement(selectedClient);
    }

    public void addClient(CustomSocket socket) {
        if (!clientListModel.contains(socket)) {
            clientListModel.addElement(socket);
        }
    }

    public void updateClient(CustomSocket socket) {
        int index = clientListModel.indexOf(socket);
        if (index != -1) {
            clientListModel.set(index, socket); // Force UI refresh
        }
    }

    public void receiveMessage(CustomSocket client, Message message) {
        if (message.isCrypted()) message.setContent("Unable to decrypt this message");
        if (clientList.getSelectedValue() == client) {
            chatArea.add(Utils.createMessageLabel(message.getContent(), false));
            chatArea.revalidate();
            chatArea.repaint();

        }
    }



}
