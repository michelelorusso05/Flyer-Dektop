import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class UploadPanel extends JPanel {
    MulticastSocket udpSocket;
    JPanel centerPanel;
    ArrayList<DeviceButton> devices;
    ActionSelectionPanel actionSelectionPanel;
    JPanel progressBarPanel;
    GridLayout progressBarGridLayout;
    int numRows;
    public UploadPanel(CardLayout cardLayout, JPanel cardsPanel, ActionSelectionPanel actionSelectionPanel) {
        setLayout(new BorderLayout());
        devices = new ArrayList<>();
        this.actionSelectionPanel = actionSelectionPanel;

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

        numRows = 1;
        progressBarGridLayout = new GridLayout(numRows, 1);
        progressBarPanel = new JPanel(progressBarGridLayout);
        JScrollPane eastScrollPanel = new JScrollPane(progressBarPanel);
        add(eastScrollPanel, BorderLayout.EAST);

        new Thread(this::listenUDP).start();
    }
    private void listenUDP() {
        System.out.println("started thread");
        try {
            if (udpSocket != null && !udpSocket.isClosed()) {
                udpSocket.close();
                Thread.sleep(1000);
            }

            udpSocket = new MulticastSocket(10468);
            InetSocketAddress group = new InetSocketAddress(InetAddress.getByName("239.255.255.250"), 10468);

            for(NetworkInterface networkInterface : Host.getActiveInterfaces())
                udpSocket.joinGroup(group, networkInterface);

            DatagramPacket received = new DatagramPacket(new byte[132], 132);

            while (true) {
                try {
                    udpSocket.receive(received);

                    byte[] data = received.getData();
                    String name = new String(data, 3, 128);
                    int port = Byte.toUnsignedInt(data[1]) + (Byte.toUnsignedInt(data[0]) << 8);
                    Host host = new Host(received.getAddress(), name, port, data[2]);
                    DeviceButton device = new DeviceButton(host);
                    this.addDevice(host, device);
                    centerPanel.updateUI();

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
        for(int i = 0; i < devices.size(); i++) {
            DeviceButton curr = devices.get(i);
            if(device.equals(curr)) {
                index = i;
                break;
            }
        }
        // New host
        if (index == -1) {
            device.addActionListener((e) -> {
                System.out.println("TCP Connection opened for this device: " + device.getHost().getPort());
                new Thread(() -> {
                    TCPConnection(device.getHost());
                }).start();
            });
            devices.add(device);
            centerPanel.add(device);
        }
        else {
            devices.get(index).setPort(found.getPort());
        }
    }
    public void TCPConnection(Host host) {
        try (Socket socket = new Socket(host.getIp(), host.getPort())) {
            File file = this.actionSelectionPanel.getSelectedFile();
            InputStream fileStream = new FileInputStream(file);

            int bytes;
            socket.setSendBufferSize(1024 * 1024);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            byte[] filenameStringBytes = file.getName().getBytes();
            byte[] mimetypeStringBytes = Files.probeContentType(file.toPath()) == null ?
                    "application/octect-stream".getBytes() : Files.probeContentType(file.toPath()).getBytes();

            // Write filename size
            dataOutputStream.writeByte(filenameStringBytes.length);
            // Write filename
            dataOutputStream.write(filenameStringBytes);
            // Write mimetype length
            dataOutputStream.writeByte(mimetypeStringBytes.length);
            // Write mimetype
            dataOutputStream.write(mimetypeStringBytes);
            final long total = file.getTotalSpace();
            // Write content size
            dataOutputStream.writeLong(total);

            byte[] buffer = new byte[1024 * 1024];

            addProgressBar(file.getName());

            while ((bytes = fileStream.read(buffer))
                    != -1) {
                dataOutputStream.write(buffer, 0, bytes);
                dataOutputStream.flush();
            }
            fileStream.close();
            dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void addProgressBar(String fileName) {
        progressBarGridLayout.setRows(numRows++);
        JPanel innerPanel = new JPanel(new GridLayout(2, 1));
        innerPanel.add(new JLabel(fileName, SwingConstants.CENTER));
        JPanel progressBarPanel = new JPanel();
        progressBarPanel.add(new JProgressBar());
        innerPanel.add(progressBarPanel);
        progressBarPanel.add(innerPanel);
        //cambia il nome coglione
    }
}