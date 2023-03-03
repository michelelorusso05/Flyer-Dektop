import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

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
            udpSocket.close();
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
                    Host host = PacketUtils.deencapsulate(received);
                    if(host.getPacketType() == 0) {
                        DeviceButton device = new DeviceButton(host);
                        this.addDevice(host, device);
                        this.centerPanel.updateUI();
                    }else {
                        for(int i = devices.size() - 1; i >= 0; i--) {
                            if(devices.get(i).equals(new DeviceButton(host))) {
                                System.out.println("sium");
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
            FileProgressBarPanel curr = this.mainFrame.uploadProgressBar.get(i);
            if(file.getName().equals(curr.getName()) && host.getIp().equals(curr.getIp())) return;
        }
        try (Socket socket = new Socket(host.getIp(), host.getPort())) {
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
            // Write content size
            final long total = file.length();
            dataOutputStream.writeLong(total);

            byte[] buffer = new byte[1024 * 1024];
            final int startSize = dataOutputStream.size();

            String progressBarFileName = file.getName();
            if(progressBarFileName.length() >= 16) {
                progressBarFileName = progressBarFileName.substring(0, 16) + "…";
            }
            FileProgressBarPanel fileProgressBar = new FileProgressBarPanel(progressBarFileName, host.getIp());
            this.mainFrame.uploadProgressBar.add(fileProgressBar);
            updateProgressBar();

            java.util.Timer timeoutTimer = new Timer();

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
                        mainFrame.uploadProgressBar.remove(fileProgressBar);
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
            }, 1000, 5000);

            while ((bytes = fileStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytes);
                dataOutputStream.flush();
                currProgress.set(dataOutputStream.size() - startSize);
                final int percentage = (int) ((float) currProgress.get() * 100f / total);

                fileProgressBar.getProgressBar().setValue(percentage);
                updateProgressBarUI();
            }
            updateProgressBar();
            fileStream.close();
            dataOutputStream.close();
        } catch (IOException e) {

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
        if(this.mainFrame.getExtendedState() == Frame.ICONIFIED && this.mainFrame.uploadProgressBar.size() == 0  && this.mainFrame.downloadProgressBar.size() == 0) {
            this.mainFrame.dispose();
            System.exit(0);
        }
    }
}