import javax.swing.*;
import java.awt.*;

public class DeviceButton extends JButton {
    public DeviceButton(String deviceName, int deviceType) {
        if(deviceName.length() > 11)
            deviceName = deviceName.substring(0, 11) + "…";

        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.setOpaque(true);
        innerPanel.setBackground(new Color(0,0,0,0));

        JLabel deviceIcon = new JLabel("", SwingConstants.CENTER);
        switch (deviceType) {
            case 0 -> deviceIcon.setIcon(new ImageIcon(PreloadedIcons.phone));
            case 1 -> deviceIcon.setIcon(new ImageIcon(PreloadedIcons.tablet));
            case 2 -> deviceIcon.setIcon(new ImageIcon(PreloadedIcons.windows));
        }
        innerPanel.add(deviceIcon, BorderLayout.CENTER);
        innerPanel.add(new JLabel(deviceName, SwingConstants.CENTER), BorderLayout.SOUTH);

        add(innerPanel);
    }
}
