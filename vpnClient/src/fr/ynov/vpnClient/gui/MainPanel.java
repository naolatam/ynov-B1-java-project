package fr.ynov.vpnClient.gui;

import fr.ynov.vpnClient.model.ClientSocket;
import fr.ynov.vpnModel.gui.ErrorFrame;
import fr.ynov.vpnModel.gui.StyleSet;
import fr.ynov.vpnModel.gui.Utils;
import fr.ynov.vpnModel.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class MainPanel extends JPanel {
    private final DefaultListModel<ClientSocket> clientListModel;
    private final JList<ClientSocket> clientList;
    private final JPanel chatArea;
    private final JTextField messageField;
    private final JLabel socketName;
    private final JButton sendButton, closeButton, deleteButton;
    private final JButton addClientButton; // New button to add a connection

    private final MainFrame mainFrame;

    public MainPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;


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

        // Button to add a new connection
        addClientButton = new JButton("+");
        addClientButton.setFont(new Font("Arial", Font.BOLD, 16));
        addClientButton.setFocusPainted(false);
        addClientButton.setBackground(StyleSet.buttonBackgroundColor);
        addClientButton.setForeground(StyleSet.buttonTextColor);
        addClientButton.addActionListener(this::addNewConnection);

        JPanel clientPanel = new JPanel(new BorderLayout());
        clientPanel.add(listScroll, BorderLayout.CENTER);
        clientPanel.add(addClientButton, BorderLayout.SOUTH);

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
        deleteButton.setForeground(StyleSet.labelTextColor);

        deleteButton.addActionListener(this::deleteSocket);
        closeButton.addActionListener(this::closeSocket);

        infoPanel.add(socketName, BorderLayout.CENTER);
        infoPanel.add(closeButton, BorderLayout.EAST);
        infoPanel.add(deleteButton, BorderLayout.WEST);

        // Séparateur entre la liste des clients et le chat
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, clientPanel, chatScroll);
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.3);

        add(splitPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        add(infoPanel, BorderLayout.NORTH);
    }

    private void addNewConnection(ActionEvent e) {
        this.mainFrame.showLoginPanel();
    }

    private void updateUIState() {
        ClientSocket selectedClient = clientList.getSelectedValue();
        if (selectedClient != null) {
            socketName.setText("Discuss: " + selectedClient);
            closeButton.setEnabled(true);
            sendButton.setEnabled(true);
            deleteButton.setEnabled(false);
            if (selectedClient.isClosed()) {
                deleteButton.setEnabled(true);
                sendButton.setEnabled(false);
                closeButton.setEnabled(false);
            }
        }else {
            socketName.setText("No discuss");
            deleteButton.setEnabled(false);
            sendButton.setEnabled(false);
            closeButton.setEnabled(false);
        }
    }

    private void loadConversation() {
        chatArea.removeAll();
        ClientSocket selectedClient = clientList.getSelectedValue();
        updateUIState();
        if (selectedClient != null) {

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
                boolean isSent = message.getOrigin() == Origin.CLIENT;
                chatArea.add(Utils.createMessageLabel(message.getContent(), isSent));
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

            selectedClient.sendMessage(message, true);

            chatArea.add(Utils.createMessageLabel(message, true));
            chatArea.revalidate();
            chatArea.repaint();

            messageField.setText("");
            messageField.grabFocus();
        }
    }

    private void closeSocket(ActionEvent e) {
        ClientSocket selectedClient = clientList.getSelectedValue();
        if (selectedClient == null) {
            ErrorFrame.showError("Unable to close connection. No socket selected.");
            return;
        }
        ConfigurationMessage cMsg = new ConfigurationMessage("CLIENT close the connection", Origin.CLIENT, false, MessageType.CLOSE, SocketConfiguration.CLOSE_CONNECTION);
        selectedClient.addMessage(cMsg);
        selectedClient.sendMessage(cMsg);
        chatArea.add(Utils.createMessageLabel(cMsg.getContent(), true));

        mainFrame.closeSocket(selectedClient);
        updateClient(selectedClient);
        updateUIState();
    }

    private void deleteSocket(ActionEvent e) {
        ClientSocket selectedClient = clientList.getSelectedValue();
        if (selectedClient == null) {
            ErrorFrame.showError("Unable to close connection. No socket selected.");
            return;
        }
        clientListModel.removeElement(selectedClient);
        updateUIState();
    }

    public void addClient(ClientSocket socket) {
        if (!clientListModel.contains(socket)) {
            clientListModel.addElement(socket);
        }
    }

    public void updateClient(ClientSocket socket) {
        int index = clientListModel.indexOf(socket);
        if (index != -1) {
            clientListModel.setElementAt(socket, index); // Force UI refresh
        }
    }

    public void receiveMessage(ClientSocket client, Message message) {
        if (message.isCrypted()) message.setContent("Unable to decrypt this message");
        if(message.getType() == MessageType.CLOSE) {
            if(client.isClosed()) {
                try {
                    client.close();
                } catch (IOException e) {
                    Utils.sleep(200);
                }
            }
            updateClient(client);
            updateUIState();
        }
        if (clientList.getSelectedValue() == client) {
            chatArea.add(Utils.createMessageLabel(message.getContent(), false));
            chatArea.revalidate();
            chatArea.repaint();
        }
    }




}
