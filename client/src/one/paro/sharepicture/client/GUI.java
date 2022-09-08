package one.paro.sharepicture.client;

import javax.swing.*;

import one.paro.sharepicture.client.gui.ConnectPanel;

public class GUI implements Runnable {
    @Override
    public void run() {
        SwingUtilities.invokeLater(() -> new ConnectPanel().setVisible(true));
    }
}
