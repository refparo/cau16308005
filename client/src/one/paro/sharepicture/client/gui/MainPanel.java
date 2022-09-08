package one.paro.sharepicture.client.gui;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;
import javax.swing.*;

import one.paro.sharepicture.client.Client;

public class MainPanel extends JFrame {
    private final Client client;

    private final FileListModel fileListModel;
    private final JList<String> fileList;

    private final JLabel picture;

    public MainPanel(String hostname, int port) {
        super();
        client = new Client(hostname, port);
        new Thread(client).start();

        this.setTitle("PictureShow");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.addWindowListener(new WindowCloseListener(client));
        this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);

        JButton refreshBtn = new JButton("刷新");
        refreshBtn.addActionListener(e -> refresh());
        constraints.gridx = 0; constraints.gridy = 0;
        constraints.weightx = 0.15; constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(refreshBtn, constraints);
        constraints.fill = GridBagConstraints.NONE;

        fileListModel = new FileListModel();
        fileList = new JList<>(fileListModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.setLayoutOrientation(JList.VERTICAL);
        fileList.setVisibleRowCount(-1);
        fileList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) showPic();
        });
        JScrollPane scrollPane = new JScrollPane(fileList);
        constraints.gridx = 0; constraints.gridy = 1;
        constraints.weightx = 0.15; constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        panel.add(scrollPane, constraints);
        constraints.fill = GridBagConstraints.NONE;

        picture = new JLabel();
        constraints.gridx = 1; constraints.gridy = 0;
        constraints.gridheight = 2;
        constraints.weightx = 1; constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        panel.add(picture, constraints);

        this.add(panel);
        refresh();
    }

    private void refresh() {
        CompletableFuture.runAsync(() -> {
            String[] filenames = client.list();
            if (filenames == null) SwingUtilities.invokeLater(() -> {
                new ConnectPanel().setVisible(true);
                this.setVisible(false);
            }); else SwingUtilities.invokeLater(() -> {
                fileListModel.setFilenames(filenames);
                picture.setIcon(null);
            });
        });
    }

    private void showPic() {
        CompletableFuture.runAsync(() -> {
            String filename = fileList.getSelectedValue();
            BufferedImage protoImage = client.show(filename);
            if (protoImage == null) {
                refresh();
                return;
            }
            Image image;
            if (protoImage.getWidth() > picture.getWidth() ||
                protoImage.getHeight() > picture.getHeight()) {
                // iw / ih > w / h
                if (protoImage.getWidth() * picture.getHeight() >
                    protoImage.getHeight() * picture.getWidth()) {
                    image = protoImage.getScaledInstance(
                        picture.getWidth(),
                        protoImage.getHeight() * picture.getWidth() / protoImage.getWidth(),
                        Image.SCALE_SMOOTH);
                } else {
                    image = protoImage.getScaledInstance(
                        protoImage.getWidth() * picture.getHeight() / protoImage.getHeight(),
                        picture.getHeight(),
                        Image.SCALE_SMOOTH);
                }
            } else image = protoImage;
            SwingUtilities.invokeLater(() -> {
                picture.setIcon(new ImageIcon(image));
                this.setTitle("PictureShow - " + filename);
            });
        });
    }

    private static class FileListModel extends AbstractListModel<String> {
        private String[] filenames = {};

        public void setFilenames(String[] filenames) {
            if (this.filenames.length > 0)
                this.fireIntervalRemoved(this, 0, this.filenames.length - 1);
            this.filenames = filenames;
            this.fireIntervalAdded(this, 0, filenames.length - 1);
        }

        @Override
        public int getSize() {
            return filenames.length;
        }

        @Override
        public String getElementAt(int i) {
            return filenames[i];
        }
    }

    private static class WindowCloseListener implements WindowListener {
        private final Client client;

        public WindowCloseListener(Client client) {
            this.client = client;
        }

        @Override
        public void windowClosing(WindowEvent windowEvent) {
            CompletableFuture.runAsync(client::close);
        }

        @Override public void windowOpened(WindowEvent windowEvent) {}
        @Override public void windowClosed(WindowEvent windowEvent) {}
        @Override public void windowIconified(WindowEvent windowEvent) {}
        @Override public void windowDeiconified(WindowEvent windowEvent) {}
        @Override public void windowActivated(WindowEvent windowEvent) {}
        @Override public void windowDeactivated(WindowEvent windowEvent) {}
    }
}
