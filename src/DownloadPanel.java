import javax.swing.*;
import java.awt.*;
import java.net.Inet4Address;
import java.net.UnknownHostException;

public class DownloadPanel extends JPanel {
    public DownloadPanel(CardLayout cardLayout, JPanel cardsPanel, ActionSelectionPanel actionSelectionPanel, MainFrame mainFrame) {
        setLayout(new BorderLayout());

        //NORTH
        JPanel northPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        JPanel northPanelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton();
        backBtn.putClientProperty( "JButton.buttonType", "roundRect" );
        backBtn.setIcon(new ImageIcon(PreloadedIcons.backArrow));
        backBtn.addActionListener((e) -> {
            cardLayout.show(cardsPanel, "selection");
        });
        JLabel searchLabel = new JLabel("Ricevi un file");
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        northPanelTop.add(backBtn);
        northPanelTop.add(searchLabel);
        northPanel.add(northPanelTop);
        add(northPanel, BorderLayout.NORTH);

        //CENTER
        JPanel devicePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel buttonPanel = new JPanel(new WrapLayout(FlowLayout.CENTER, 150, 0));

        JLabel deviceName;
        try {
            deviceName = new JLabel("Il dispositivo è visibile come: " + Inet4Address.getLocalHost().getHostName(), SwingConstants.CENTER);
            buttonPanel.add(deviceName);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        JLabel deviceIcon = new JLabel("", SwingConstants.CENTER);
        devicePanel.addComponentListener(new LabelResizeListener("./res/windows.svg", deviceIcon));
        devicePanel.addComponentListener(new LabelResizeTextListener(deviceName, 13));
        buttonPanel.add(deviceIcon);

        devicePanel.add(buttonPanel, gbc);
        add(devicePanel, BorderLayout.CENTER);

        //SOUTH
        JPanel southPanel = new JPanel(new GridLayout(2, 1));
        JLabel wifiIcon = new JLabel();
        wifiIcon.setIcon(new ImageIcon(PreloadedIcons.wifi));

        JPanel wifiPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel wifiWarning = new JLabel("Questo dispositivo è ora visibile a tutti i dispositivi connessi alla tua stessa rete WiFi.");
        wifiWarning.setFont(new Font("Arial", Font.PLAIN, 20));
        wifiPanel.add(wifiIcon);
        wifiPanel.add(wifiWarning);

        JPanel folderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel folderIcon = new JLabel();
        folderIcon.setIcon(new ImageIcon(PreloadedIcons.folder));
        JLabel folderWarning = new JLabel("I file ricevuti saranno scaricati nella cartella Download.");
        folderWarning.setFont(new Font("Arial", Font.PLAIN, 20));
        folderPanel.add(folderIcon);
        folderPanel.add(folderWarning);

        southPanel.add(wifiPanel);
        southPanel.add(folderPanel);
        add(southPanel, BorderLayout.SOUTH);
    }
}
