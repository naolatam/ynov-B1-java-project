package fr.ynov.vpnServer.gui;

import fr.ynov.vpnModel.gui.ErrorFrame;
import fr.ynov.vpnModel.gui.StyleSet;
import fr.ynov.vpnModel.gui.Utils;
import fr.ynov.vpnModel.model.*;
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
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
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

    private void resetUIState() {
        socketName.setText("No discuss");
        sendButton.setEnabled(false);
        closeButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    private void loadConversation() {
        chatArea.removeAll();
        updateUIState();

        CustomSocket selectedClient = clientList.getSelectedValue();
        if (selectedClient != null) {
            selectedClient.getMessages().forEach(this::updateChatUI);
        }
        updateChatArea();

    }

    private void updateChatUI(Message message) {
        if (message.getType() == MessageType.CONFIG) {
            handleConfigMessage(message);
        } else {
            addMessageAndUpdateUI(message.getContent(), message.getOrigin() == Origin.SERVER);
        }
    }

    private void handleConfigMessage(Message message) {
        ConfigurationMessage configMsg = (ConfigurationMessage) message;
        switch (configMsg.getConfiguration()) {
            case SET_NAME ->
                    chatArea.add(Utils.createConfigMessageLabel(message.getOrigin().name() + " set name: " + message.getContent()));
            case GET_PUBLIC_KEY, SEND_PUBLIC_KEY ->
                    chatArea.add(Utils.createConfigMessageLabel(message.getOrigin().name() + " sent a key"));
        }
    }

    private void sendMessage(ActionEvent e) {
        CustomSocket selectedClient = clientList.getSelectedValue();
        if (selectedClient == null || selectedClient.getSocket().isClosed()) {
            ErrorFrame.showError("Cannot send message. The socket is disconnected.");
            return;
        }

        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            try {
                selectedClient.sendMessage(message, true);
                addMessageAndUpdateUI(message, true);
            } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException ex) {
                ErrorFrame.showError("Failed to send message: " + ex.getMessage());
            }

            messageField.setText("");
            messageField.requestFocus();
        }
    }

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

    public void disconnectSocket(CustomSocket client) {
        ConfigurationMessage closeMsg = new ConfigurationMessage("CLIENT disconnected", Origin.CLIENT, false, MessageType.CLOSE, SocketConfiguration.CLOSE_CONNECTION);
        client.addMessage(closeMsg);
        updateClient(client);
        if (client == clientList.getSelectedValue()) {
            addMessageAndUpdateUI(closeMsg.getContent(), false);
        }
    }

    private void deleteSocket(ActionEvent e) {
        CustomSocket selectedClient = clientList.getSelectedValue();
        if (selectedClient != null) {
            clientListModel.removeElement(selectedClient);
            updateUIState();
            chatArea.removeAll();
            updateChatArea();
        }
    }

    public void addClient(CustomSocket socket) {
        if (!clientListModel.contains(socket)) {
            clientListModel.addElement(socket);
        }
    }

    public void updateClient(CustomSocket socket) {
        int index = clientListModel.indexOf(socket);
        if (index != -1) {
            clientListModel.setElementAt(socket, index);
        }
    }

    public void receiveMessage(CustomSocket client, Message message) {
        if (message.isCrypted()) {
            message.setContent("Cannot decrypt this message");
        }

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

    private void addMessageAndUpdateUI(String message, boolean isSent) {
        chatArea.add(Utils.createMessageLabel(message, isSent));
        updateChatArea();
    }

    private void updateChatArea() {
        chatArea.revalidate();
        chatArea.repaint();
    }
}
