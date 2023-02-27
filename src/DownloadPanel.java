import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadPanel extends JPanel {
    ActionSelectionPanel actionSelectionPanel;
    MainFrame mainFrame;
    ArrayList<MulticastSocket> sockets;
    Timer beaconTimer;
    ServerSocket serverSocket;
    static Socket socket;
    static AtomicInteger currentPort = new AtomicInteger(0);
    public DownloadPanel(CardLayout cardLayout, JPanel cardsPanel, ActionSelectionPanel actionSelectionPanel, MainFrame mainFrame) {
        setLayout(new BorderLayout());
        this.actionSelectionPanel = actionSelectionPanel;
        this.mainFrame = mainFrame;
        initSocket();
        startBeacon();

        //NORTH
        JPanel northPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        JPanel northPanelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton();
        backBtn.putClientProperty( "JButton.buttonType", "roundRect" );
        backBtn.setIcon(new ImageIcon(PreloadedIcons.backArrow));
        backBtn.addActionListener((e) -> {
            if (beaconTimer != null) beaconTimer.cancel();
            // Dispose of the socket
            if (sockets != null) {
                for (MulticastSocket socket : sockets) {
                    socket.close();
                }
            }
            try {
                serverSocket.close();
            } catch (IOException ev) {
                throw new RuntimeException(ev);
            }
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

    private void initSocket() {
        try {
            serverSocket = new ServerSocket(0);
            currentPort.set(serverSocket.getLocalPort());

            new Thread(() -> {
                try {
                    while (true) {
                        socket = serverSocket.accept();
                        onConnectionReceived();
                    }
                } catch (IOException ignored) {}
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static Socket consumeSocket() {
        Socket s = socket;
        socket = null;
        return s;
    }
    private void onConnectionReceived() {
        new Thread(this::TCPConnection).start();
    }
    private void startBeacon() {
        // Create UDP station
        try {
            ArrayList<NetworkInterface> interfaces = Host.getActiveInterfaces();
            sockets = new ArrayList<>(interfaces.size());
            for (NetworkInterface networkInterface : interfaces) {
                MulticastSocket socket = new MulticastSocket();
                socket.setNetworkInterface(networkInterface);
                socket.setTimeToLive(1);
                sockets.add(socket);
            }

            byte[] send = new byte[132];
            String hostname = Host.getHostname();
            send[2] = (byte)2;
            byte[] name = hostname.substring(0, Math.min(64, hostname.length())).getBytes();
            System.arraycopy(name, 0, send, 3, name.length);

            InetSocketAddress group = new InetSocketAddress(InetAddress.getByName("239.255.255.250"), 10468);

            beaconTimer = new Timer();
            beaconTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        int portNum = currentPort.get();
                        send[0] = (byte) ((portNum >>> 8) & 255);
                        send[1] = (byte) (portNum & 255);

                        DatagramPacket packet = new DatagramPacket(send, send.length, group);

                        for (MulticastSocket socket : sockets)
                            socket.send(packet);
                    } catch (IOException ignored) {}
                }
            }, 0, 2000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void TCPConnection() {
        try (Socket socket = consumeSocket()) {
            socket.setSoTimeout(5000);
            socket.setReceiveBufferSize(1024 * 1024);
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            int bytes;

            int filenameLength = dataInputStream.readByte();
            byte[] filenameBuffer = new byte[filenameLength];
            dataInputStream.read(filenameBuffer);

            int mimetypeLength = dataInputStream.readByte();
            byte[] mimetypeBuffer = new byte[mimetypeLength];
            dataInputStream.read(mimetypeBuffer);

            String filename = new String(filenameBuffer);
            String mimeType = new String(mimetypeBuffer);

            System.out.println(this.actionSelectionPanel.getSelectedDirectory().toString() + File.separator + filename);
            OutputStream fileOutputStream = new FileOutputStream(
                    this.actionSelectionPanel.getSelectedDirectory().toString() + File.separator + filename
            );

            long size = dataInputStream.readLong();
            long total = size;

            System.out.println(size);

            byte[] buffer = new byte[1024 * 1024];
            int prevPercentage = 0;
            long elapsedMillis = System.currentTimeMillis();
            while (size > 0
                    && (bytes = dataInputStream.read(
                    buffer, 0,
                    (int)Math.min(buffer.length, size)))
                    != -1) {
                fileOutputStream.write(buffer, 0, bytes);
                size -= bytes;

                final long progress = total - size;
                final int percentage = (int) ((float) progress * 100f / total);
            }

            dataInputStream.close();
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
