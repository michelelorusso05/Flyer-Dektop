import javax.swing.*;
import java.awt.*;

public class DeviceButton extends JButton {
    public DeviceButton(String deviceName, int deviceType) {
        if(deviceName.length() > 11) {
            deviceName = deviceName.substring(0, 11);
            deviceName += "...";
        }

        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.setOpaque(true);
        innerPanel.setBackground(new Color(0,0,0,0));
        JLabel deviceIcon = new JLabel("", SwingConstants.CENTER);
        deviceIcon.setIcon(new ImageIcon(PreloadedIcons.phone));
        innerPanel.add(deviceIcon, BorderLayout.CENTER);
        innerPanel.add(new JLabel(deviceName, SwingConstants.CENTER), BorderLayout.SOUTH);
        add(innerPanel);
    }
}
