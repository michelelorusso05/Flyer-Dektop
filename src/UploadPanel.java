import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.ArrayList;

public class UploadPanel extends JPanel {
    MulticastSocket udpSocket;
    JPanel centerPanel;
    ArrayList<DeviceButton> devices;
    ActionSelectionPanel actionSelectionPanel;
    static JPanel progressBarPanel;
    static WrapLayout progressBarWrapLayout;
    MainFrame mainFrame;
    public UploadPanel(CardLayout cardLayout, JPanel cardsPanel, ActionSelectionPanel actionSelectionPanel, MainFrame mainFrame) {
        setLayout(new BorderLayout());

        this.devices = new ArrayList<>();
        this.actionSelectionPanel = actionSelectionPanel;
        this.mainFrame = mainFrame;
        UploadPanel.progressBarWrapLayout = new WrapLayout();
        UploadPanel.progressBarPanel = new JPanel(progressBarWrapLayout);

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
        JLabel fileName = new JLabel(actionSelectionPanel.getSelectedFile().getName());
        fileName.setFont(new Font("Arial", Font.PLAIN, 20));
        genericFileIcon.setIcon(new ImageIcon(PreloadedIcons.genericFile));
        northPanelBottom.add(genericFileIcon);
        northPanelBottom.add(fileName);
        northPanel.add(northPanelBottom);

        add(northPanel, BorderLayout.NORTH);

        centerPanel = new JPanel(new WrapLayout(FlowLayout.CENTER, 25, 25));
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
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

        JScrollPane eastScrollPanel = new JScrollPane(progressBarPanel);
        eastScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        eastScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(eastScrollPanel, BorderLayout.EAST);

        loadProgressBar();
        updateProgressBarUI();
        new Thread(this::listenUDP).start();
    }
    private void listenUDP() {
        try {
            if (this.udpSocket != null && !this.udpSocket.isClosed()) {
                this.udpSocket.close();
                Thread.sleep(1000);
            }

            this.udpSocket = new MulticastSocket(10468);
            InetSocketAddress group = new InetSocketAddress(InetAddress.getByName("239.255.255.250"), 10468);

            for(NetworkInterface networkInterface : Host.getActiveInterfaces())
                this.udpSocket.joinGroup(group, networkInterface);

            DatagramPacket received = new DatagramPacket(new byte[132], 132);

            while (true) {
                try {
                    this.udpSocket.receive(received);

                    byte[] data = received.getData();
                    String name = new String(data, 3, 128);
                    int port = Byte.toUnsignedInt(data[1]) + (Byte.toUnsignedInt(data[0]) << 8);
                    Host host = new Host(received.getAddress(), name, port, data[2]);
                    DeviceButton device = new DeviceButton(host);
                    this.addDevice(host, device);
                    this.centerPanel.updateUI();

                } catch (SocketException e) {
                    System.err.println("Socket destroyed");
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void addDevice(Host found, DeviceButton device) {
        int index = -1;
        for(int i = 0; i < this.devices.size(); i++) {
            DeviceButton curr = this.devices.get(i);
            if(device.equals(curr)) {
                index = i;
                break;
            }
        }
        // New host
        if (index == -1) {
            device.addActionListener((e) -> {
                new Thread(() -> {
                    TCPConnection(device.getHost());
                }).start();
            });
            this.devices.add(device);
            this.centerPanel.add(device);
        }
        else {
            this.devices.get(index).setPort(found.getPort());
        }
    }
    public void TCPConnection(Host host) {
        File file = this.actionSelectionPanel.getSelectedFile();
        for(int i = 0; i < this.mainFrame.uploadProgressBar.size(); i++) {
            if(file.getName().equals(this.mainFrame.uploadProgressBar.get(i).getName())) return;
        }
        try (Socket socket = new Socket(host.getIp(), host.getPort())) {
            InputStream fileStream = new FileInputStream(file);

            int bytes;
            socket.setSendBufferSize(1024 * 1024);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            byte[] filenameStringBytes = file.getName().getBytes();
            System.out.println(Files.probeContentType(file.toPath()));
            byte[] mimetypeStringBytes = Files.probeContentType(file.toPath()) == null ?
                    "application/octet-stream".getBytes() : Files.probeContentType(file.toPath()).getBytes();

            // Write filename size
            dataOutputStream.writeByte(filenameStringBytes.length);
            // Write filename
            dataOutputStream.write(filenameStringBytes);
            // Write mimetype length
            dataOutputStream.writeByte(mimetypeStringBytes.length);
            // Write mimetype
            dataOutputStream.write(mimetypeStringBytes);
            // Write content size
            final long total = file.length();
            dataOutputStream.writeLong(total);

            byte[] buffer = new byte[1024 * 1024];
            final int startSize = dataOutputStream.size();

            FileProgressBarPanel fileProgressBar = new FileProgressBarPanel(file.getName());
            this.mainFrame.uploadProgressBar.add(fileProgressBar);
            updateProgressBar();

            while ((bytes = fileStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytes);
                dataOutputStream.flush();
                final int progress = dataOutputStream.size() - startSize;
                final int percentage = (int) ((float) progress * 100f / total);

                fileProgressBar.getProgressBar().setValue(percentage);
                updateProgressBarUI();
            }
            updateProgressBar();
            fileStream.close();
            dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void updateProgressBarUI() {
        UploadPanel.progressBarPanel.revalidate();
        UploadPanel.progressBarPanel.repaint();
        UploadPanel.progressBarPanel.updateUI();
        revalidate();
        repaint();
        updateUI();
    }
    public void loadProgressBar() {
        for(int i = 0; i < this.mainFrame.uploadProgressBar.size(); i++) {
            FileProgressBarPanel curr = this.mainFrame.uploadProgressBar.get(i);
            if(curr.getProgressBar().getValue() < 100) {
                UploadPanel.progressBarPanel.add(curr);
            }
        }
        for(int i = this.mainFrame.uploadProgressBar.size() - 1; i >= 0; i--) {
            FileProgressBarPanel curr = this.mainFrame.uploadProgressBar.get(i);
            if(curr.getProgressBar().getValue() >= 100) {
                this.mainFrame.uploadProgressBar.remove(i);
            }
        }
        updateProgressBarUI();
    }
    public void updateProgressBar() {
        UploadPanel.progressBarPanel.removeAll();
        loadProgressBar();
    }
}