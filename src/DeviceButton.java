import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class DeviceButton extends JButton {
    private final Host host;
    private final JLabel deviceIcon;
    private final JLabel deviceNameLabel;
    private long lastUpdate;
    public DeviceButton(Host host) {
        this.host = host;
        String deviceName = this.host.getName();
        if(deviceName.length() > 11)
            deviceName = deviceName.substring(0, 11) + "…";

        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.setOpaque(true);
        innerPanel.setBackground(new Color(0,0,0,0));

        this.deviceIcon = new JLabel("", SwingConstants.CENTER);
        //Imposta l'icona del pulsante in base al tipo di dispositivo
        switch (this.host.getType()) {
            case 0:
                deviceIcon.setIcon(new ImageIcon(PreloadedIcons.phone));
                break;
            case 1:
                deviceIcon.setIcon(new ImageIcon(PreloadedIcons.tablet));
                break;
            case 2:
                deviceIcon.setIcon(new ImageIcon(PreloadedIcons.windows));
                break;
        }
        innerPanel.add(deviceIcon, BorderLayout.CENTER);
        this.deviceNameLabel = new JLabel(deviceName, SwingConstants.CENTER);
        innerPanel.add(deviceNameLabel, BorderLayout.SOUTH);

        add(innerPanel);
    }

    /**
     * Returns the host on which is based the button
     * @return Host host
     * */
    public Host getHost() {return host;}
    /**
     * Returns the label on which is loaded the icon of the device
     * @return JLabel icon
     * */
    public JLabel getDeviceIcon() {return deviceIcon;}
    /**
     * Returns the label of the davice name
     * @return JLabel deviceName
     * */
    public JLabel getDeviceNameLabel() {return deviceNameLabel;}
    /**
     * Sets the new port by updating the port of the host
     * @param port the new port
     * */
    public void setPort(int port) {this.host.updatePort(port);}
    /**
     * Compares two device buttons
     * @param device device to compare
     * @return boolean isEqual
     * */
    public boolean equals(DeviceButton device) {
        return (this.host.getType() == device.getHost().getType()) &&
                (this.host.getIp().equals(device.getHost().getIp()));
    }
    /**
     * Sets the last time since the device button was updated
     * @param millis the milliseconds of the last update
     * */
    public void setLastUpdate(long millis) {this.lastUpdate = millis;}
    /**
     * Returns the last time since the device button was updated
     * @return long millis
     * */
    public long getLastUpdate() {return lastUpdate;}
}
