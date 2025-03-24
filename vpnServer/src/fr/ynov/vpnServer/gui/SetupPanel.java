package fr.ynov.vpnServer.gui;

import com.sun.jdi.VoidType;
import fr.ynov.vpnModel.gui.ErrorFrame;
import fr.ynov.vpnModel.gui.SuccessFrame;
import fr.ynov.vpnModel.gui.StyleSet;
import fr.ynov.vpnServer.model.CustomServerSocket;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.function.Function;


public class SetupPanel extends JPanel {

    private JSpinner spPort;
    private JButton btnStart;
    private final MainFrame mf;

    public SetupPanel(MainFrame parent) {
        this.mf = parent;
        setLayout(new GridBagLayout());
        setBackground(StyleSet.backgroundColor); // Dark background

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel title = new JLabel("Server setup");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(StyleSet.titleTextColor);
        add(title, gbc);

        gbc.gridwidth = 0;
        gbc.gridy++;
        JLabel subTitle = new JLabel("On wich port you want to run the server socket?");
        subTitle.setFont(new Font("Arial", Font.BOLD, 16));
        subTitle.setForeground(StyleSet.titleTextColor);
        add(subTitle, gbc);



        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblPort = new JLabel("Port:");
        lblPort.setForeground(StyleSet.labelTextColor);
        add(lblPort, gbc);

        gbc.gridx = 1;
        spPort = new JSpinner(new SpinnerNumberModel(1024, 0, 49151, 1));
        spPort.setFont(new Font("Arial", Font.PLAIN, 14));
        spPort.setForeground(StyleSet.inputTextColor);
        spPort.setBackground(StyleSet.inputBackgroundColor);
        spPort.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(spPort, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        btnStart = new JButton("Start");
        styleButton(btnStart);
        add(btnStart, gbc);

        btnStart.addActionListener(this::connectToServer);

    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(StyleSet.buttonTextColor);
        button.setBackground(StyleSet.buttonBackgroundColor); // Blue
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
    }

    private void connectToServer(ActionEvent e) {
        SwingUtilities.invokeLater(() ->  btnStart.setText("Starting..."));
        SwingUtilities.invokeLater(() ->  btnStart.setEnabled(false));

        int port = Integer.parseInt(spPort.getValue().toString());

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {

                try {
                    CustomServerSocket socket = new CustomServerSocket( port );
                    mf.setServerSocket(socket);
                    return 0; // Succès
                } catch (IOException ex) {
                    return -1; // Échec
                } catch (Exception ex) {
                    return -1;
                }
            }

            @Override
            protected void done() {
                try {
                    Integer success = get();
                    if (success==0) {
                        SuccessFrame.showSuccess("Server socket started on port: " + port);
                        mf.showMainPanel();
                    } else if(success==-1) {
                        ErrorFrame.showError("Unable to connect using port:" +  port + "!");
                    }
                } catch (Exception ex) {
                    ErrorFrame.showError("Unexcepted error : " + ex.getMessage());
                }
                SwingUtilities.invokeLater(() ->  btnStart.setText("Start"));
                SwingUtilities.invokeLater(() ->  btnStart.setEnabled(true));
            }
        }.execute();

    }
}
