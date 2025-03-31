package fr.ynov.vpnServer.gui;

import fr.ynov.vpnModel.gui.ErrorFrame;
import fr.ynov.vpnModel.gui.StyleSet;
import fr.ynov.vpnModel.gui.Utils;
import fr.ynov.vpnModel.model.ConfigurationMessage;
import fr.ynov.vpnModel.model.Message;
import fr.ynov.vpnModel.model.MessageType;
import fr.ynov.vpnModel.model.Origin;
import fr.ynov.vpnModel.model.SocketConfiguration;
import fr.ynov.vpnServer.model.CustomSocket;

import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;

/**
 * MainPanel represents the main user interface panel for managing client connections and chat messages.
 * It displays the connected clients, chat messages, and provides controls for sending messages,
 * closing connections, and suppressing conversation.
 */
public class MainPanel extends JPanel {
    private final DefaultListModel<CustomSocket> clientListModel;
    private final JList<CustomSocket> clientList;
    private final JPanel chatArea;
    private final JTextField messageField;
    private final JLabel socketName;
    private final JButton sendButton, closeButton, deleteButton;
    private final GridBagConstraints chatAreaGBC = new GridBagConstraints();


    /**
     * Constructs the MainPanel and initializes UI components.
     */
    public MainPanel() {
        setLayout(new BorderLayout());

        clientListModel = new DefaultListModel<>();
        clientList = initializeClientList();
        chatArea = initializeChatArea();
        messageField = new JTextField();
        sendButton = createStyledButton("Send", this::sendMessage, false);
        closeButton = createStyledButton("Close", this::closeSocket, false);
        deleteButton = createStyledButton("Delete", this::deleteSocket, false);
        deleteButton.setBackground(StyleSet.deleteButtonBackground);

        socketName = new JLabel("No discuss");
        socketName.setForeground(StyleSet.titleTextColor);

        addComponents();

        // Init the grid constraints for chatArea
        chatAreaGBC.gridx = 0;
        chatAreaGBC.gridy = 0;
        chatAreaGBC.weightx = 1.0;
        chatAreaGBC.anchor = GridBagConstraints.NORTH;

    }

    /**
     * Adds a new client to the client list if not already present.
     *
     * @param socket The {@link CustomSocket} to be added
     */
    public void addClient(CustomSocket socket) {
        if (!clientListModel.contains(socket)) {
            clientListModel.addElement(socket);
        }
    }


    /**
     * Handles client disconnection by sending a close message and updating the UI.
     *
     * @param client The disconnected client
     */
    public void disconnectSocket(CustomSocket client) {
        // Send a new fake message to say the CLIENT has been disconnected.
        ConfigurationMessage closeMsg = new ConfigurationMessage("CLIENT disconnected", Origin.CLIENT, false, MessageType.CLOSE, SocketConfiguration.CLOSE_CONNECTION);
        client.addMessage(closeMsg);
        updateClient(client);
        if (client == clientList.getSelectedValue()) {
            addMessageAndUpdateUI(closeMsg.getContent(), false);
        }
    }

    /**
     * Handles receiving a message from a client and updating the UI accordingly.
     *
     * @param client  The {@link CustomSocket} the message
     * @param message The received {@link Message}
     */
    public void receiveMessage(CustomSocket client, Message message) {
        if (message.isCrypted()) {
            message.setContent("Cannot decrypt this message");
        }

        // If message is announcing the client will close the socket, try close it too
        if (message.getType() == MessageType.CLOSE && !client.getSocket().isClosed()) {
            try {
                client.getSocket().close();
            } catch (IOException e) {
                if (!client.getSocket().isClosed()) {
                    System.err.println("Failed to close socket: " + e.getMessage());
                }
            }
        }
        updateUIState();
        if (clientList.getSelectedValue() == client) {
            addMessageAndUpdateUI(message.getContent(), false);
        }
    }


    /**
     * Updates the displayed client list when a client's information changes.
     *
     * @param socket The {@link CustomSocket} whose information has changed
     */
    public void updateClient(CustomSocket socket) {
        int index = clientListModel.indexOf(socket);
        if (index != -1) {
            clientListModel.setElementAt(socket, index);
        }
    }

    /**
     * Updates the UI button state and information label based on the selected client.
     */
    public void updateUIState() {
        CustomSocket selectedClient = clientList.getSelectedValue();
        if (selectedClient != null) {
            socketName.setText("Discussion: " + selectedClient);
            boolean isSocketClosed = selectedClient.getSocket().isClosed();
            sendButton.setEnabled(!isSocketClosed);
            closeButton.setEnabled(!isSocketClosed);
            deleteButton.setEnabled(isSocketClosed);
        } else {
            resetUIState();
        }
    }


    private JList<CustomSocket> initializeClientList() {
        JList<CustomSocket> list = new JList<>(clientListModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(e -> loadConversation());
        list.setBackground(StyleSet.backgroundColor);
        list.setForeground(StyleSet.labelTextColor);
        return list;
    }

    private JPanel initializeChatArea() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(StyleSet.backgroundColor);
        panel.setForeground(StyleSet.titleTextColor);
        return panel;
    }

    private void addComponents() {
        JScrollPane listScroll = new JScrollPane(clientList);
        listScroll.setPreferredSize(new Dimension(200, 0));

        JScrollPane chatScroll = new JScrollPane(chatArea);

        JPanel inputPanel = initializeInputPanel();
        JPanel infoPanel = initializeInfoPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, chatScroll);
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.3);

        add(splitPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        add(infoPanel, BorderLayout.NORTH);
    }

    private JPanel initializeInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(StyleSet.backgroundColor);
        messageField.setBackground(StyleSet.backgroundColor);
        messageField.setForeground(StyleSet.labelTextColor);

        panel.add(messageField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);
        return panel;
    }

    private JPanel initializeInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(StyleSet.backgroundColor);

        panel.add(socketName, BorderLayout.CENTER);
        panel.add(closeButton, BorderLayout.EAST);
        panel.add(deleteButton, BorderLayout.WEST);

        return panel;
    }

    private JButton createStyledButton(String text, java.awt.event.ActionListener action, boolean enabled) {
        JButton button = new JButton(text);
        button.setEnabled(enabled);
        StyleSet.styleButton(button);
        button.addActionListener(action);
        return button;
    }

    /**
     * Resets the UI state when no client is selected.
     */
    private void resetUIState() {
        socketName.setText("No discuss");
        sendButton.setEnabled(false);
        closeButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    /**
     * Loads the conversation history for the selected client socket.
     */
    private void loadConversation() {
        // Clear the chatArea and update UI
        chatArea.removeAll();
        updateUIState();
        chatAreaGBC.anchor = GridBagConstraints.NORTH;


        // Load history message from the select socket
        CustomSocket selectedClient = clientList.getSelectedValue();
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
            addMessageAndUpdateUI(message.getContent(), message.getOrigin() == Origin.SERVER);
        }
    }

    /**
     * Handles configuration messages and insert it in the chat UI.
     *
     * @param message The {@link Message}.
     */
    private void handleConfigMessage(Message message) {
        if(!(message instanceof ConfigurationMessage)) {return;}
        ConfigurationMessage configMsg = (ConfigurationMessage) message;
        switch (configMsg.getConfiguration()) {
            case SET_NAME ->
                    chatArea.add(Utils.createConfigMessageLabel(message.getOrigin().name() + " set name: " + message.getContent()), chatAreaGBC);
            case GET_PUBLIC_KEY, SEND_PUBLIC_KEY ->
                    chatArea.add(Utils.createConfigMessageLabel(message.getOrigin().name() + " sent a key"), chatAreaGBC);
        }
        chatAreaGBC.gridy++;
    }

    /**
     * Sends a message to the selected client.
     *
     * @param e The action event triggering the send operation
     */
    private void sendMessage(ActionEvent e) {
        CustomSocket selectedClient = clientList.getSelectedValue();
        if (selectedClient == null || selectedClient.getSocket().isClosed()) {
            ErrorFrame.showError("Cannot send message. The socket is disconnected.");
            return;
        }

        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            selectedClient.sendMessage(message, true);
            addMessageAndUpdateUI(message, true);

            messageField.setText("");
            messageField.requestFocus();
        }
    }

    /**
     * Closes the selected client's socket connection.
     *
     * @param e The action event triggering the close operation
     */
    private void closeSocket(ActionEvent e) {
        CustomSocket selectedClient = clientList.getSelectedValue();
        if (selectedClient == null) {
            ErrorFrame.showError("No socket selected.");
            return;
        }
        try {
            ConfigurationMessage closeMsg = new ConfigurationMessage("SERVER closed the connection", Origin.SERVER, false, MessageType.CLOSE, SocketConfiguration.CLOSE_CONNECTION);
            selectedClient.addMessage(closeMsg);
            selectedClient.sendMessage(closeMsg);
            selectedClient.getSocket().close();
            updateUIState();
            addMessageAndUpdateUI(closeMsg.getContent(), true);
            updateClient(selectedClient);
        } catch (IOException ex) {
            ErrorFrame.showError("Error closing socket: " + ex.getMessage());
        }
    }

    /**
     * Delete the selected discussion if the socket is closed.
     *
     * @param e The action event triggering the delete operation
     */
    private void deleteSocket(ActionEvent e) {
        CustomSocket selectedClient = clientList.getSelectedValue();
        if (selectedClient != null) {
            clientListModel.removeElement(selectedClient);
            chatArea.removeAll();
            updateUIState();
            repaintChatArea();
        }
    }

    /**
     * Adds a new message to the chat area and updates the UI.
     *
     * @param message The message content.
     * @param isSent  Whether the message was sent by the user.
     */
    private void addMessageAndUpdateUI(String message, boolean isSent) {
        chatAreaGBC.anchor = isSent?GridBagConstraints.WEST:GridBagConstraints.EAST;
        chatArea.add(Utils.createMessageLabel(message, isSent), chatAreaGBC);
        repaintChatArea();
        chatAreaGBC.gridy++;
    }

    /**
     * Refreshes the chat area UI.
     */
    private void repaintChatArea() {
        chatArea.revalidate();
        chatArea.repaint();
    }

}
