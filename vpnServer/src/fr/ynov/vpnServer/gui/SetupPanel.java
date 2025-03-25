package fr.ynov.vpnServer.gui;

import com.sun.jdi.VoidType;
import fr.ynov.vpnModel.gui.ErrorFrame;
import fr.ynov.vpnModel.gui.SuccessFrame;
import fr.ynov.vpnModel.gui.StyleSet;
import fr.ynov.vpnServer.model.CustomServerSocket;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.function.Function;


public class SetupPanel extends JPanel {

    private JTextField txtKey, txtName;
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

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblKey = new JLabel("AES Key:");
        lblKey.setForeground(StyleSet.labelTextColor);
        add(lblKey, gbc);

        gbc.gridx = 1;
        txtKey = new JTextField(15);
        styleTextField(txtKey);
        add(txtKey, gbc);
        txtKey.setEditable(false);
        txtKey.setText(Base64.getEncoder().encodeToString(generateKey().getEncoded()));

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblName = new JLabel("Server name:");
        lblName.setForeground(StyleSet.labelTextColor);
        add(lblName, gbc);

        gbc.gridx = 1;
        txtName = new JTextField(15);
        styleTextField(txtName);
        add(txtName, gbc);

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

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setForeground(StyleSet.inputTextColor);
        field.setBackground(StyleSet.inputBackgroundColor);
        field.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    private void connectToServer(ActionEvent e) {
        SwingUtilities.invokeLater(() ->  btnStart.setText("Starting..."));
        SwingUtilities.invokeLater(() ->  btnStart.setEnabled(false));

        int port = Integer.parseInt(spPort.getValue().toString());

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {

                try {
                    CustomServerSocket socket = new CustomServerSocket( port, txtName.getText() );
                    socket.setPrivateKey(new SecretKeySpec(Base64.getDecoder().decode(txtKey.getText()), "AES"));
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

    private SecretKey generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");

            // Initialize the KeyGenerator with a key size (128, 192, or 256 bits)
            keyGen.init(256);  // AES supports key sizes: 128, 192, or 256 bits

            // Generate the AES secret key
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    };

}
