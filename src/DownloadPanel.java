import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TimerTask;
import java.util.Timer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DownloadPanel extends JPanel {
    ActionSelectionPanel actionSelectionPanel;
    Timer updateProgressBarTimer = new Timer();
    MainFrame mainFrame;
    static ArrayList<MulticastSocket> sockets;
    Timer beaconTimer;
    ServerSocket serverSocket;
    static Socket socket;
    static AtomicInteger currentPort = new AtomicInteger(0);
    static JPanel progressBarPanel;
    static WrapLayout progressBarWrapLayout;
    static InetSocketAddress group;

    static {
        try {
            group = new InetSocketAddress(InetAddress.getByName("239.255.255.250"), 10468);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public DownloadPanel(CardLayout cardLayout, JPanel cardsPanel, ActionSelectionPanel actionSelectionPanel, MainFrame mainFrame) {
        setLayout(new BorderLayout());
        this.actionSelectionPanel = actionSelectionPanel;
        this.mainFrame = mainFrame;
        DownloadPanel.progressBarWrapLayout = new WrapLayout();
        DownloadPanel.progressBarPanel = new JPanel(progressBarWrapLayout);
        initSocket();
        startBeacon();
        updateProgressBarTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(mainFrame.downloadProgressBar.size() > 0) {
                    for(int i = mainFrame.downloadProgressBar.size() - 1; i >= 0; i--) {
                        if(mainFrame.downloadProgressBar.get(i).getNeedUpdate()) {
                            updateProgressBar();
                        }
                    }
                }
            }
        }, 0, 10);

        //NORTH
        JPanel northPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        JPanel northPanelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton();
        backBtn.putClientProperty( "JButton.buttonType", "roundRect" );
        backBtn.setIcon(new ImageIcon(PreloadedIcons.backArrow));
        backBtn.addActionListener((e) -> {
            try {
                if (beaconTimer != null) beaconTimer.cancel();
                // Dispose of the socket
                if (sockets != null) {
                    sendForgetMeMessage();
                }
                serverSocket.close();
                updateProgressBarTimer.cancel();
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
        devicePanel.addComponentListener(new LabelResizeListener(this.getClass().getResource("windows.svg").toString(), deviceIcon));
        devicePanel.addComponentListener(new LabelResizeTextListener(deviceName, 13));
        buttonPanel.add(deviceIcon);

        devicePanel.add(buttonPanel, gbc);
        add(devicePanel, BorderLayout.CENTER);

        //SOUTH
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel wifiIcon = new JLabel();
        wifiIcon.setIcon(new ImageIcon(PreloadedIcons.wifi));

        JLabel wifiWarning = new JLabel("Questo dispositivo è ora visibile a tutti i dispositivi connessi alla tua stessa rete WiFi.");
        wifiWarning.setFont(new Font("Arial", Font.PLAIN, 20));
        southPanel.add(wifiIcon);
        southPanel.add(wifiWarning);

        add(southPanel, BorderLayout.SOUTH);

        JScrollPane eastScrollPanel = new JScrollPane(progressBarPanel);
        eastScrollPanel.setBorder(null);
        eastScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        eastScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(eastScrollPanel, BorderLayout.EAST);

        loadProgressBar();
        updateProgressBarUI();
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
            beaconTimer = new Timer();
            beaconTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        byte[] toSend = PacketUtils.encapsulate(currentPort.get(), 2, 0, Host.getHostname());
                        DatagramPacket packet = new DatagramPacket(toSend, toSend.length, group);
                        for (MulticastSocket socket : sockets)
                            socket.send(packet);
                    } catch (IOException ignored) {}
                }
            }, 0, 1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void TCPConnection() {
        try (Socket socket = consumeSocket()){
            socket.setSoTimeout(10000);
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

            OutputStream fileOutputStream = Files.newOutputStream(Paths.get(this.actionSelectionPanel.getSelectedDirectory().toString() + File.separator + filename));

            long size = dataInputStream.readLong();
            long total = size;

            String progressBarFileName = filename;
            if(progressBarFileName.length() >= 16) {
                progressBarFileName = progressBarFileName.substring(0, 16) + "…";
            }
            FileProgressBarPanel fileProgressBar = new FileProgressBarPanel(progressBarFileName, null);
            this.mainFrame.downloadProgressBar.add(fileProgressBar);
            updateProgressBar();

            byte[] buffer = new byte[1024 * 1024];
            Timer timeoutTimer = new Timer();

            AtomicLong currProgress = new AtomicLong(0);
            AtomicLong prevProgress = new AtomicLong(1);
            timeoutTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        if(prevProgress.get() == currProgress.get() && currProgress.get() < total) {
                            throw new IOException("read request timed out");
                        }else {
                            prevProgress.set(currProgress.get());
                        }
                    }catch (IOException e) {
                        fileProgressBar.setFailed();
                        updateProgressBar();
                        JOptionPane.showMessageDialog(mainFrame,
                                "Non è stato possibile ricevere il file: " + filename,
                                "Socket Timeout",
                                JOptionPane.ERROR_MESSAGE);
                        try {
                            socket.close();
                            this.cancel();
                        } catch (IOException ex) {
                            this.cancel();
                        }
                    }
                }
            }, 1000, 10000);
            while (size > 0
                    && (bytes = dataInputStream.read(
                    buffer, 0,
                    (int)Math.min(buffer.length, size))) != -1) {
                fileOutputStream.write(buffer, 0, bytes);
                size -= bytes;

                currProgress.set(total - size);
                final int percentage = (int) ((float) currProgress.get() * 100f / total);
                fileProgressBar.getProgressBar().setValue(percentage);
                updateProgressBarUI();
            }
            fileProgressBar.setCompleted();
            updateProgressBar();
            dataInputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            System.err.println("Socket timeout");
            e.printStackTrace();
        }
    }
    public void updateProgressBarUI() {
        DownloadPanel.progressBarPanel.revalidate();
        DownloadPanel.progressBarPanel.repaint();
        DownloadPanel.progressBarPanel.updateUI();
        revalidate();
        repaint();
        updateUI();
    }
    public void loadProgressBar() {
        for(int i = 0; i < this.mainFrame.downloadProgressBar.size(); i++) {
            FileProgressBarPanel curr = this.mainFrame.downloadProgressBar.get(i);
            if(!curr.getIsClosed()) {
                DownloadPanel.progressBarPanel.add(curr);
            }
        }
        for(int i = this.mainFrame.downloadProgressBar.size() - 1; i >= 0; i--) {
            FileProgressBarPanel curr = this.mainFrame.downloadProgressBar.get(i);
            if(curr.getIsClosed()) {
                this.mainFrame.downloadProgressBar.remove(i);
            }
        }
        updateProgressBarUI();
    }
    public void updateProgressBar() {
        DownloadPanel.progressBarPanel.removeAll();
        loadProgressBar();
        if(this.mainFrame.getExtendedState() == Frame.ICONIFIED && this.mainFrame.uploadProgressBar.size() == 0  && this.mainFrame.downloadProgressBar.size() == 0) {
            this.mainFrame.dispose();
            try {
                DownloadPanel.sendForgetMeMessage();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.exit(0);
        }
        if(this.mainFrame.getExtendedState() == Frame.ICONIFIED) {
            for(int i = 0; i < this.mainFrame.uploadProgressBar.size(); i++) {
                FileProgressBarPanel curr = this.mainFrame.uploadProgressBar.get(i);
                if(curr.getProgressBar().getValue() == 100 || curr.getIsFailed()) continue;
                return;
            }
            for(int i = 0; i < this.mainFrame.downloadProgressBar.size(); i++) {
                FileProgressBarPanel curr = this.mainFrame.downloadProgressBar.get(i);
                if(curr.getProgressBar().getValue() == 100 || curr.getIsFailed()) continue;
                return;
            }
            this.mainFrame.dispose();
            try {
                DownloadPanel.sendForgetMeMessage();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.exit(0);
        }
    }
    public static void sendForgetMeMessage() throws IOException {
        byte[] toSend = PacketUtils.encapsulate(currentPort.get(), 2, 1, Host.getHostname());
        DatagramPacket packet = new DatagramPacket(toSend, toSend.length, group);
        if(sockets == null) return;
        for (MulticastSocket socket : sockets) {
            if(socket.isClosed()) continue;
            socket.send(packet);
            socket.close();
        }
    }
}