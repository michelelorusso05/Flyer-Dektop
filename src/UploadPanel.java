import javax.swing.*;
import java.awt.*;

public class UploadPanel extends JPanel {
    public UploadPanel(CardLayout cardLayout, JPanel cardsPanel) {
        setLayout(new BorderLayout());

        JPanel northPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        JPanel northPanelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton();
        backBtn.putClientProperty( "JButton.buttonType", "roundRect" );
        backBtn.setIcon(new ImageIcon(PreloadedIcons.backArrow));
        backBtn.addActionListener((e) -> {
            cardLayout.show(cardsPanel, "selection");
        });
        JLabel searchLabel = new JLabel("Ricerca in corso...");
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        JLabel spinner = new JLabel();
        spinner.setIcon(PreloadedIcons.spinner);
        northPanelTop.add(backBtn);
        northPanelTop.add(searchLabel);
        northPanelTop.add(spinner);
        northPanel.add(northPanelTop);

        JPanel northPanelBottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel genericFileIcon = new JLabel();
        JLabel fileName = new JLabel("NomeFile.estensione");
        fileName.setFont(new Font("Arial", Font.PLAIN, 20));
        genericFileIcon.setIcon(new ImageIcon(PreloadedIcons.genericFile));
        northPanelBottom.add(genericFileIcon);
        northPanelBottom.add(fileName);
        northPanel.add(northPanelBottom);

        add(northPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new WrapLayout(FlowLayout.CENTER, 25, 25));
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        for(int i = 0; i < 10; i++) {
            centerPanel.add(new DeviceButton("ciao" + i, i % 3));
        }
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JLabel wifiIcon = new JLabel();
        wifiIcon.setIcon(new ImageIcon(PreloadedIcons.wifi));
        JLabel wifiWarning = new JLabel("Assicurati che il dispositivo ricevente sia connesso alla tua stessa rete WiFi.");
        wifiWarning.setFont(new Font("Arial", Font.PLAIN, 20));
        southPanel.add(wifiIcon);
        southPanel.add(wifiWarning);
        add(southPanel, BorderLayout.SOUTH);
    }
}