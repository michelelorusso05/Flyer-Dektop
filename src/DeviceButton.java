import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class DeviceButton extends JButton {
    private final Host host;
    private JLabel deviceIcon;
    private JLabel deviceNameLabel;
    public DeviceButton(Host host) {
        this.host = host;
        String deviceName = this.host.getName();
        if(deviceName.length() > 11)
            deviceName = deviceName.substring(0, 11) + "…";

        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.setOpaque(true);
        innerPanel.setBackground(new Color(0,0,0,0));

        this.deviceIcon = new JLabel("", SwingConstants.CENTER);
        switch (this.host.getType()) {
            case 0 -> deviceIcon.setIcon(new ImageIcon(PreloadedIcons.phone));
            case 1 -> deviceIcon.setIcon(new ImageIcon(PreloadedIcons.tablet));
            case 2 -> deviceIcon.setIcon(new ImageIcon(PreloadedIcons.windows));
        }
        innerPanel.add(deviceIcon, BorderLayout.CENTER);
        this.deviceNameLabel = new JLabel(deviceName, SwingConstants.CENTER);
        innerPanel.add(deviceNameLabel, BorderLayout.SOUTH);

        add(innerPanel);
    }

    public Host getHost() {return host;}
    public JLabel getDeviceIcon() {return deviceIcon;}
    public JLabel getDeviceNameLabel() {return deviceNameLabel;}
    public void setPort(int port) {this.host.updatePort(port);}
    public boolean equals(DeviceButton device) {
        return (this.host.getType() == device.getHost().getType()) &&
                (this.host.getIp().equals(device.getHost().getIp()));
    }
}
