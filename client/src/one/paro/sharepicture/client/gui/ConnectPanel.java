package one.paro.sharepicture.client.gui;

import java.awt.*;
import javax.swing.*;

public class ConnectPanel extends JFrame {
    private final JTextField hostnameField;
    private final JTextField portField;

    public ConnectPanel() {
        super();
        this.setTitle("PictureShow - 连接");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.getRootPane().setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);

        JLabel hostnameLabel = new JLabel("主机", SwingConstants.RIGHT);
        constraints.gridx = 0; constraints.gridy = 0;
        panel.add(hostnameLabel, constraints);

        hostnameField = new JTextField("localhost", 20);
        constraints.gridx = 1; constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(hostnameField, constraints);
        constraints.fill = GridBagConstraints.NONE;

        JLabel portLabel = new JLabel("端口", SwingConstants.RIGHT);
        constraints.gridx = 0; constraints.gridy = 1;
        panel.add(portLabel, constraints);

        portField = new JTextField("30110", 5);
        constraints.gridx = 1; constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(portField, constraints);
        constraints.fill = GridBagConstraints.NONE;

        JButton connectBtn = new JButton("连接");
        connectBtn.addActionListener(e -> connect());
        constraints.gridx = 0; constraints.gridy = 2;
        constraints.gridwidth = 2;
        panel.add(connectBtn, constraints);

        this.add(panel);
        this.pack();
    }

    private void connect() {
        int port;
        try {
            port = Integer.parseInt(portField.getText());
        } catch (NumberFormatException e) {
            portField.setBorder(BorderFactory.createLineBorder(Color.RED));
            return;
        }
        new MainPanel(hostnameField.getText(), port).setVisible(true);
        this.setVisible(false);
    }
}
