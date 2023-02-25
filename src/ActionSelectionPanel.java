import javax.swing.*;
import java.awt.*;

public class ActionSelectionPanel extends JPanel {
    public ActionSelectionPanel(CardLayout cardLayout, JPanel cardsPanel) {
        SpringLayout layout = new SpringLayout();
        setLayout(layout);

        // Add components
        //Upload Button
        JButton upload = new JButton();
        JPanel innerUploadBtnPanel = new JPanel(new BorderLayout());
        innerUploadBtnPanel.setOpaque(true);
        innerUploadBtnPanel.setBackground(new Color(0,0,0,0));

        JLabel uploadIcon = new JLabel("", SwingConstants.CENTER);
        upload.addComponentListener(new LabelResizeListener("./res/upload.svg", uploadIcon));
        innerUploadBtnPanel.add(uploadIcon, BorderLayout.CENTER);

        JLabel uploadLabel = new JLabel("Invia", SwingConstants.CENTER);
        upload.addComponentListener(new LabelResizeTextListener(uploadLabel));
        innerUploadBtnPanel.add(uploadLabel, BorderLayout.SOUTH);

        upload.add(innerUploadBtnPanel);

        upload.addActionListener((e) -> {
            cardLayout.show(cardsPanel, "upload");
        });

        //Download Button
        JButton download = new JButton();
        JPanel innerDownloadBtnPanel = new JPanel(new BorderLayout());
        innerDownloadBtnPanel.setOpaque(true);
        innerDownloadBtnPanel.setBackground(new Color(0,0,0,0));

        JLabel downloadIcon = new JLabel("", SwingConstants.CENTER);
        download.addComponentListener(new LabelResizeListener("./res/download.svg", downloadIcon));
        innerDownloadBtnPanel.add(downloadIcon, BorderLayout.CENTER);

        JLabel downloadLabel = new JLabel("Ricevi", SwingConstants.CENTER);
        download.addComponentListener(new LabelResizeTextListener(downloadLabel));
        innerDownloadBtnPanel.add(downloadLabel, BorderLayout.SOUTH);

        download.add(innerDownloadBtnPanel);

        add(upload);
        add(download);

        // Set constraints
        layout.putConstraint(SpringLayout.WEST, upload, 16, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, upload, 16, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.EAST, upload, -16, SpringLayout.HORIZONTAL_CENTER, this);
        layout.putConstraint(SpringLayout.SOUTH, upload, -16, SpringLayout.SOUTH, this);

        layout.putConstraint(SpringLayout.WEST, download, 16, SpringLayout.HORIZONTAL_CENTER, this);
        layout.putConstraint(SpringLayout.NORTH, download, 16, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.EAST, download, -16, SpringLayout.EAST, this);
        layout.putConstraint(SpringLayout.SOUTH, download, -16, SpringLayout.SOUTH, this);
    }
}
