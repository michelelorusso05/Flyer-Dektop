import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

public class UploadPanel extends JPanel {
    Timer updateProgressBarTimer = new Timer();
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

        updateProgressBarTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(mainFrame.uploadProgressBar.size() > 0) {
                    for(int i = mainFrame.uploadProgressBar.size() - 1; i >= 0; i--) {
                        if(mainFrame.uploadProgressBar.get(i).getNeedUpdate()) {
                            mainFrame.uploadProgressBar.get(i).setNeedUpdate(false);
                            updateProgressBar();
                        }
                    }
                }
            }
        }, 0, 10);

        JPanel northPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        JPanel northPanelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton();
        backBtn.putClientProperty( "JButton.buttonType", "roundRect" );
        backBtn.setIcon(new ImageIcon(PreloadedIcons.backArrow));
        backBtn.addActionListener((e) -> {
            udpSocket.close();
            updateProgressBarTimer.cancel();
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
        genericFileIcon.setIcon(FileMappings.getIconFromFilename(actionSelectionPanel.getSelectedFile()));
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
        eastScrollPanel.setBorder(null);
        eastScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        eastScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(eastScrollPanel, BorderLayout.EAST);

        loadProgressBar();
        updateProgressBarUI();
        new Thread(this::listenUDP).start();
        Timer deleteNotRespondingDevicesTimer = new Timer();
        deleteNotRespondingDevicesTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                deleteNotRespondigDevices();
            }
        }, 0, 5000);
    }
    private void listenUDP() {
        try {
            if (this.udpSocket != null && !this.udpSocket.isClosed()) {
                this.udpSocket.close();
                Thread.sleep(1000);
            }

            this.udpSocket = new MulticastSocket(10468);
            InetSocketAddress group = new InetSocketAddress(InetAddress.getByName("224.0.0.255"), 10468);

            for(NetworkInterface networkInterface : Host.getActiveInterfaces())
                this.udpSocket.joinGroup(group, networkInterface);

            DatagramPacket received = new DatagramPacket(new byte[132], 132);

            while (true) {
                try {
                    this.udpSocket.receive(received);
                    System.out.println("device found");
                    Host host = PacketUtils.deencapsulate(received);
                    if(host.getPacketType() == 0) {
                        DeviceButton device = new DeviceButton(host);
                        this.addDevice(host, device);
                        this.centerPanel.updateUI();
                    }else {
                        for(int i = devices.size() - 1; i >= 0; i--) {
                            if(devices.get(i).equals(new DeviceButton(host))) {
                                this.centerPanel.remove(devices.get(i));
                                devices.remove(i);
                                updateProgressBarUI();
                            }
                        }
                    }

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
            device.setLastUpdate(System.currentTimeMillis());
            this.devices.add(device);
            this.centerPanel.add(device);
        }
        else {
            this.devices.get(index).setPort(found.getPort());
            this.devices.get(index).setLastUpdate(System.currentTimeMillis());
        }
    }
    public void TCPConnection(Host host) {
        File file = this.actionSelectionPanel.getSelectedFile();
        for(int i = 0; i < this.mainFrame.uploadProgressBar.size(); i++) {
            FileProgressBarPanel curr = this.mainFrame.uploadProgressBar.get(i);
            if(file.getName().equals(curr.getName()) && host.getIp().equals(curr.getIp())) return;
        }
        FileProgressBarPanel currProgressBar = null;
        try (Socket socket = new Socket(host.getIp(), host.getPort())) {
            InputStream fileStream = Files.newInputStream(file.toPath());

            int bytes;
            socket.setSendBufferSize(1024 * 1024);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            byte[] filenameStringBytes = file.getName().getBytes();
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

            FileProgressBarPanel fileProgressBar = new FileProgressBarPanel(file.getName(), host.getIp(), socket);
            currProgressBar = fileProgressBar;
            this.mainFrame.uploadProgressBar.add(fileProgressBar);
            updateProgressBar();

            java.util.Timer timeoutTimer = new Timer();

            AtomicLong written = new AtomicLong(0);
            AtomicLong prevProgress = new AtomicLong(0);
            timeoutTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(fileProgressBar.getIsCanceled()) return;
                    try {
                        if(prevProgress.get() == written.get() && written.get() < total) {
                            throw new IOException("read request timed out");
                        }else {
                            prevProgress.set(written.get());
                        }
                    }catch (IOException e) {
                        fileProgressBar.setFailed();
                        updateProgressBar();
                        JOptionPane.showMessageDialog(mainFrame,
                                "Non è stato possibile inviare il file: " + file.getName(),
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

            long startTime = System.currentTimeMillis();
            long acc = 0;
            while ((bytes = fileStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytes);
                dataOutputStream.flush();
                written.set(written.get() + bytes);
                final int percentage = (int) ((float) written.get() * 100f / total);
                acc += bytes;

                if(System.currentTimeMillis() - startTime >= 1000) {
                    float speed = (float)acc / 1000;
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    String speedText = speed > 1000 ?
                            decimalFormat.format((speed / 1000)) + "MB/s" :
                            decimalFormat.format(speed) + "kB/s";
                    currProgressBar.setTransferSpeed(speedText);
                    updateProgressBar();
                    acc = 0;
                    startTime = System.currentTimeMillis();
                }

                fileProgressBar.getProgressBar().setValue(percentage);
                updateProgressBarUI();
            }
            fileProgressBar.getProgressBar().setValue(100);
            fileProgressBar.setCompleted();
            updateProgressBar();
            fileStream.close();
            dataOutputStream.close();
        } catch (IOException e) {
            if(currProgressBar != null) {
                if(currProgressBar.getIsCanceled() || currProgressBar.getIsFailed()) return;
                if(currProgressBar.getProgressBar().getValue() < 100) {
                    currProgressBar.setCanceled();
                    return;
                }
            }
            for(int i = devices.size() - 1; i >= 0; i--) {
                if(devices.get(i).equals(new DeviceButton(host))) {
                    this.centerPanel.remove(devices.get(i));
                    devices.remove(i);
                    updateProgressBarUI();
                }
            }
            JOptionPane.showMessageDialog(mainFrame,
                    "Non è stato possibile connettersi all'host: " + host.getName(),
                    "Host unreachable",
                    JOptionPane.ERROR_MESSAGE);
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
            if(!curr.getIsClosed()) {
                UploadPanel.progressBarPanel.add(curr);
            }
        }
        for(int i = this.mainFrame.uploadProgressBar.size() - 1; i >= 0; i--) {
            FileProgressBarPanel curr = this.mainFrame.uploadProgressBar.get(i);
            if(curr.getIsClosed()) {
                this.mainFrame.uploadProgressBar.remove(i);
            }
        }
        updateProgressBarUI();
    }
    public void updateProgressBar() {
        UploadPanel.progressBarPanel.removeAll();
        loadProgressBar();
        if(this.mainFrame.getExtendedState() == Frame.ICONIFIED && this.mainFrame.uploadProgressBar.size() == 0  && this.mainFrame.downloadProgressBar.size() == 0) {
            this.mainFrame.dispose();
            System.exit(0);
        }
        if(this.mainFrame.getExtendedState() == Frame.ICONIFIED) {
            for(int i = 0; i < this.mainFrame.uploadProgressBar.size(); i++) {
                FileProgressBarPanel curr = this.mainFrame.uploadProgressBar.get(i);
                if(curr.getProgressBar().getValue() == 100 || curr.getIsFailed() || curr.getIsCanceled()) continue;
                return;
            }
            for(int i = 0; i < this.mainFrame.downloadProgressBar.size(); i++) {
                FileProgressBarPanel curr = this.mainFrame.downloadProgressBar.get(i);
                if(curr.getProgressBar().getValue() == 100 || curr.getIsFailed() || curr.getIsCanceled()) continue;
                return;
            }
            this.mainFrame.dispose();
            System.exit(0);
        }
    }
    public void deleteNotRespondigDevices() {
        for(int i = devices.size() - 1; i >= 0; i--) {
            if(System.currentTimeMillis() - devices.get(i).getLastUpdate() > 5000) {
                this.centerPanel.remove(devices.get(i));
                devices.remove(devices.get(i));
                updateProgressBarUI();
            }
        }
    }
}