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

        // Chat zone
        chatArea = new JPanel();
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.Y_AXIS));
        chatArea.setBackground(StyleSet.backgroundColor);
        chatArea.setForeground(StyleSet.titleTextColor);
        JScrollPane chatScroll = new JScrollPane(chatArea);

        // Field to enter message and button to send it
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        sendButton = new JButton("Send");

        messageField.setBackground(StyleSet.backgroundColor);
        messageField.setForeground(StyleSet.labelTextColor);
        inputPanel.setBackground(StyleSet.backgroundColor);
        sendButton.setEnabled(false);

        StyleSet.styleButton(sendButton);

        // Call the sendMessage method when the button is pressed.
        sendButton.addActionListener(this::sendMessage);

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Information and utils button like close and delete discussion
        // Also adding a label for showing actual selected discussion
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

        // Adding event listener for this two new button
        deleteButton.addActionListener(this::deleteSocket);
        closeButton.addActionListener(this::closeSocket);

        infoPanel.add(socketName, BorderLayout.CENTER);
        infoPanel.add(closeButton, BorderLayout.EAST);
        infoPanel.add(deleteButton, BorderLayout.WEST);

        // Spliter between chat list and chat area
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, clientPanel, chatScroll);
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.3);

        add(splitPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        add(infoPanel, BorderLayout.NORTH);
    }

    public void addClient(ClientSocket socket) {
        if (!clientListModel.contains(socket)) {
            clientListModel.addElement(socket);
        }
    }

    // This method is used to update the UI and send a fake local message when server is disconnected
    public void disconnectSocket(ClientSocket client) {
        ConfigurationMessage cMsg = new ConfigurationMessage("SERVER disconnect", Origin.SERVER, false, MessageType.CLOSE, SocketConfiguration.CLOSE_CONNECTION);
        client.addMessage(cMsg);
        updateClient(client);
        if (client == clientList.getSelectedValue()) {
            addMessageAndUpdateUI(cMsg.getContent(), false);
        }
    }

    // This method is used to update the UI and show the new received message
    public void receiveMessage(ClientSocket client, Message message) {
        if (message.isCrypted()) message.setContent("Unable to decrypt this message");

        if (message.getType() == MessageType.CLOSE && !client.isClosed()) {
            try {
                client.close();
            } catch (IOException e) {
                if (!client.isClosed()) {
                    System.err.println("Failed to close socket: " + e.getMessage());
                }
            }
            updateClient(client);
        }
        updateUIState();

        if (clientList.getSelectedValue() == client) {
            addMessageAndUpdateUI(message.getContent(), false);
        }
    }

    // This method is used to update a ClientSocket name in the list
    public void updateClient(ClientSocket socket) {
        int index = clientListModel.indexOf(socket);
        if (index != -1) {
            clientListModel.setElementAt(socket, index); // Force UI refresh
        }
    }

    // This method is used to show the login panel when the + button is pressed
    private void addNewConnection(ActionEvent e) {
        this.mainFrame.showLoginPanel();
    }

    // This method is used to update all button.
    // It's set the button enabled or disabled depending on the current selection and socket state
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
        } else {
            socketName.setText("No discuss");
            deleteButton.setEnabled(false);
            sendButton.setEnabled(false);
            closeButton.setEnabled(false);
        }
    }

    // This method is used to load a conversation when the selected socket change
    private void loadConversation() {
        // Clear the chatArea
        chatArea.removeAll();
        updateUIState();

        ClientSocket selectedClient = clientList.getSelectedValue();
        if (selectedClient != null) {
            selectedClient.getMessages().forEach(this::updateChatUI);
        }
        repaintChatArea();

    }

    // This method is used to add a new message in the chatArea depending of the message type (config/message)
    private void updateChatUI(Message message) {
        if (message.getType() == MessageType.CONFIG) {
            handleConfigMessage(message);
        } else {
            addMessageAndUpdateUI(message.getContent(), message.getOrigin() == Origin.CLIENT);
        }
    }

    // This method is used to add new configuration message in the chatArea
    private void handleConfigMessage(Message message) {
        // Cast Message instance to ConfigurationMessage
        ConfigurationMessage configMsg = (ConfigurationMessage) message;
        switch (configMsg.getConfiguration()) {
            case SET_NAME ->
                    chatArea.add(Utils.createConfigMessageLabel(message.getOrigin().name() + " set name: " + message.getContent()));
            case GET_PUBLIC_KEY, SEND_PUBLIC_KEY ->
                    chatArea.add(Utils.createConfigMessageLabel(message.getOrigin().name() + " sent a key"));
        }
    }

    // This method is used to send a message when send button is clicked
    private void sendMessage(ActionEvent e) {
        ClientSocket selectedClient = clientList.getSelectedValue();
        if (selectedClient == null || selectedClient.isClosed()) {
            ErrorFrame.showError("Unable to send message. This socket is disconnected.");
            return;
        }
        if (!messageField.getText().trim().isEmpty()) {
            String message = messageField.getText().trim();

            // Send a new crypted message
            selectedClient.sendMessage(message, true);

            updateUIState();

            addMessageAndUpdateUI(message, true);

            // Clear the message input and focus on it.
            messageField.setText("");
            messageField.grabFocus();
        }
    }

    // This method is used to close the actual selected socket and prevent the server from this close
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

    // This method is used to remove a conversation from the list
    private void deleteSocket(ActionEvent e) {
        ClientSocket selectedClient = clientList.getSelectedValue();
        if (selectedClient == null) {
            ErrorFrame.showError("Unable to close connection. No socket selected.");
            return;
        }
        clientListModel.removeElement(selectedClient);
        updateUIState();
    }

    // This method is used to add a new message inside the chatArea and update the ui
    private void addMessageAndUpdateUI(String message, boolean isSent) {
        chatArea.add(Utils.createMessageLabel(message, isSent));
        repaintChatArea();
    }

    // This method is used to refresh chatArea UI
    private void repaintChatArea() {
        chatArea.revalidate();
        chatArea.repaint();
    }

}
