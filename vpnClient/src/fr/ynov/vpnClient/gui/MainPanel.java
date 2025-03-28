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

/**
 * The MainPanel class represents the main user interface panel for the VPN client.
 * It manages client connections, displays messages, and provides user controls
 * for sending messages, closing connections, and deleting conversations.
 */
public class MainPanel extends JPanel {

    /**
     * The list model containing connected clients.
     */
    private final DefaultListModel<ClientSocket> clientListModel;

    /**
     * The list component displaying connected clients.
     */
    private final JList<ClientSocket> clientList;
    private final JPanel chatArea;
    private final JTextField messageField;
    private final JLabel socketName;
    private final JButton sendButton, closeButton, deleteButton;
    private final JButton addClientButton; // New button to add a connection

    private final MainFrame mainFrame;

    /**
     * Constructs a MainPanel instance and initializes the UI components.
     *
     * @param mainFrame The main frame of the application.
     */
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

    /**
     * Adds a client socket to the client list if it is not already present.
     *
     * @param socket The {@link fr.ynov.vpnClient.model.ClientSocket} to be added.
     */
    public void addClient(ClientSocket socket) {
        if (!clientListModel.contains(socket)) {
            clientListModel.addElement(socket);
        }
    }

    /**
     * Handles server disconnection by sending a close message and updating the UI.
     *
     * @param client The {@link fr.ynov.vpnClient.model.ClientSocket} to be disconnected.
     */
    public void disconnectSocket(ClientSocket client) {
        ConfigurationMessage cMsg = new ConfigurationMessage("SERVER disconnect", Origin.SERVER, false, MessageType.CLOSE, SocketConfiguration.CLOSE_CONNECTION);
        client.addMessage(cMsg);
        updateClient(client);
        if (client == clientList.getSelectedValue()) {
            addMessageAndUpdateUI(cMsg.getContent(), false);
        }
    }

    /**
     * Processes received messages and updates the UI accordingly.
     *
     * @param client  The {@link fr.ynov.vpnClient.model.ClientSocket} that sent the message.
     * @param message The received {@link fr.ynov.vpnModel.model.Message}.
     */
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

    /**
     * Updates the {@link fr.ynov.vpnClient.model.ClientSocket} in the client list UI.
     *
     * @param socket The {@link fr.ynov.vpnClient.model.ClientSocket} to be updated.
     */
    public void updateClient(ClientSocket socket) {
        int index = clientListModel.indexOf(socket);
        if (index != -1) {
            clientListModel.setElementAt(socket, index); // Force UI refresh
        }
    }

    /**
     * Displays the login panel when the "Add Client" button is clicked.
     *
     * @param e The action event.
     */
    private void addNewConnection(ActionEvent e) {
        this.mainFrame.showLoginPanel();
    }

    /**
     * Updates the information label and button state based on the selected client socket.
     */
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

    /**
     * Loads the conversation history for the selected client socket.
     */
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

    /**
     * Insert a message in the chat UI.
     *
     * @param message The {@link fr.ynov.vpnModel.model.Message} to be displayed.
     */
    private void updateChatUI(Message message) {
        if (message.getType() == MessageType.CONFIG) {
            handleConfigMessage(message);
        } else {
            addMessageAndUpdateUI(message.getContent(), message.getOrigin() == Origin.CLIENT);
        }
    }

    /**
     * Handles configuration messages and insert it in the chat UI.
     *
     * @param message The configuration message.
     */
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

    /**
     * Handle "send" button click event by sending a message to the select server
     *
     * @param e The action event triggered by clicking the send button.
     */
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

    /**
     * Closes the currently selected socket and notifies the server.
     *
     * @param e The action event triggered by clicking the close button.
     */
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

    /**
     * Removes the selected client socket from the list.
     *
     * @param e The action event triggered by clicking the delete button.
     */
    private void deleteSocket(ActionEvent e) {
        ClientSocket selectedClient = clientList.getSelectedValue();
        if (selectedClient == null) {
            ErrorFrame.showError("Unable to close connection. No socket selected.");
            return;
        }
        clientListModel.removeElement(selectedClient);
        updateUIState();
    }

    /**
     * Adds a new message to the chat area and updates the UI.
     *
     * @param message The message content.
     * @param isSent  Whether the message was sent by the user.
     */
    private void addMessageAndUpdateUI(String message, boolean isSent) {
        chatArea.add(Utils.createMessageLabel(message, isSent));
        repaintChatArea();
    }

    /**
     * Refreshes the chat area UI.
     */
    private void repaintChatArea() {
        chatArea.revalidate();
        chatArea.repaint();
    }

}
