import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
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

    private static final String deviceNameString;
    private static JLabel searchLabel;
    private static JLabel deviceName;
    private static JLabel wifiWarning;
    private static JButton clearBtn;
    private static String receivedFileError = "Non è stato possibile ricevere il file: ";

    static {
        //salva appena viene avviata la JVM il nome del dispositivo e il gruppo di multcast a cui connettersi
        try {
            deviceNameString = Inet4Address.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        try {
            group = new InetSocketAddress(InetAddress.getByName("224.0.0.255"), 10468);
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
        //Viene aperto il socket verso cui verrà inviato il file
        initSocket();
        //Viene attivato il beacon che manda pacchetti di advertisement ogni secondo
        startBeacon();
        //Viene creato il timer che aggiorna ogni 10 millisecondi le progressbar
        updateProgressBarTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(mainFrame.downloadProgressBar.size() > 0) {
                    for(int i = mainFrame.downloadProgressBar.size() - 1; i >= 0; i--) {
                        if(mainFrame.downloadProgressBar.get(i).getNeedUpdate()) {
                            mainFrame.downloadProgressBar.get(i).setNeedUpdate(false);
                            updateProgressBar();
                        }
                    }
                } else {
                    if (clearBtn != null)
                        clearBtn.setVisible(false);
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
        searchLabel = new JLabel("Ricevi un file");
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        northPanelTop.add(backBtn);
        northPanelTop.add(searchLabel);
        northPanel.add(northPanelTop);
        add(northPanel, BorderLayout.NORTH);

        //CENTER
        JPanel devicePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel buttonPanel = new JPanel(new WrapLayout(FlowLayout.CENTER, 150, 0));

        deviceName = new JLabel("Il dispositivo è visibile come: " + deviceNameString, SwingConstants.CENTER);
        buttonPanel.add(deviceName);

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

        wifiWarning = new JLabel("Questo dispositivo è ora visibile a tutti i dispositivi connessi alla tua stessa rete WiFi.");
        wifiWarning.setFont(new Font("Arial", Font.PLAIN, 20));
        southPanel.add(wifiIcon);
        southPanel.add(wifiWarning);

        add(southPanel, BorderLayout.SOUTH);

        JPanel eastPanel = new JPanel(new BorderLayout());
        clearBtn = new JButton("Pulisci");
        clearBtn.setVisible(mainFrame.downloadProgressBar.size() != 0);
        clearBtn.addActionListener(e -> {
            boolean hasRemovedSomething = false;
            for(int i = mainFrame.downloadProgressBar.size() - 1; i >= 0; i--) {
                FileProgressBarPanel curr = mainFrame.downloadProgressBar.get(i);
                if(curr.getIsFailed() || curr.getIsCanceled() || curr.getIsCompleted()) {
                    mainFrame.downloadProgressBar.remove(curr);
                    hasRemovedSomething = true;
                }
            }
            if(!hasRemovedSomething) return;
            clearBtn.setVisible(false);
            updateProgressBar();
        });

        JScrollPane eastScrollPanel = new JScrollPane(progressBarPanel);
        eastScrollPanel.setBorder(null);
        eastScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        eastScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        eastPanel.add(eastScrollPanel, BorderLayout.CENTER);
        eastPanel.add(clearBtn, BorderLayout.SOUTH);
        add(eastPanel, BorderLayout.EAST);

        loadProgressBar();
        updateProgressBarUI();
        changeLanguage();
    }
    /**
     * Creates a new server socket and actively waits until some devices starts the TCP connection
     * */
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
    /**
     * Consumes the last socket
     * @return Socket socket
     * */
    public static Socket consumeSocket() {
        Socket s = socket;
        socket = null;
        return s;
    }
    /**
     * Starts a new thread with the flow protocol by accepting the TCP connection
     * */
    private void onConnectionReceived() {
        new Thread(this::TCPConnection).start();
    }
    /**
     * Starts a timer where every one second sends in the multicast group the advertisement packet
     * */
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
    /**
     * Starts a TCP connection
     * */
    public void TCPConnection() {
        FileProgressBarPanel currProgressBar = null;
        File file = null;
        try (Socket socket = consumeSocket()){
            socket.setReceiveBufferSize(1024 * 1024);
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            //Legge tutti i campi del pacchetto flow
            int version = dataInputStream.readByte();
            int type = dataInputStream.readByte();

            int deviceNameLength = dataInputStream.readInt();
            byte[] deviceNameBuffer = new byte[deviceNameLength];
            dataInputStream.read(deviceNameBuffer);

            int filenameLength = dataInputStream.readInt();
            byte[] filenameBuffer = new byte[filenameLength];
            dataInputStream.read(filenameBuffer);

            int mimetypeLength = dataInputStream.readInt();
            byte[] mimetypeBuffer = new byte[mimetypeLength];
            dataInputStream.read(mimetypeBuffer);

            String filename = new String(filenameBuffer);
            file = new File(this.actionSelectionPanel.getSelectedDirectory().toString()
                    + File.separator + filename);

            OutputStream fileOutputStream = Files.newOutputStream(Paths.get(this.actionSelectionPanel.getSelectedDirectory().toString() +
                    File.separator + filename));

            long size = dataInputStream.readLong();
            long total = size;

            FileProgressBarPanel fileProgressBar = new FileProgressBarPanel(this.actionSelectionPanel.getSelectedDirectory().toString(),
                    filename, null, socket, true, new String(deviceNameBuffer));
            currProgressBar = fileProgressBar;
            this.mainFrame.downloadProgressBar.add(fileProgressBar);
            if(clearBtn != null)
                clearBtn.setVisible(true);
            updateProgressBar();

            byte[] buffer = new byte[1024 * 1024];
            Timer timeoutTimer = new Timer();

            AtomicLong currProgress = new AtomicLong(0);
            AtomicLong prevProgress = new AtomicLong(1);
            //Controlla se i byte ricevuti sia cambiato negli ultimi 10 secondi altrimenti scatta la SocketException
            timeoutTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(fileProgressBar.getIsCanceled()) return;
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
                                receivedFileError + filename,
                                "Socket Timeout",
                                JOptionPane.ERROR_MESSAGE);
                        try {
                            socket.close();
                            this.cancel();
                            throw new SocketException();
                        } catch (IOException ex) {
                            this.cancel();
                        }
                    }
                }
            }, 1000, 10000);
            long startTime = System.currentTimeMillis();
            long acc = 0;
            int bytes;
            while (size > 0) {

                bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size));
                if (bytes == -1) throw new SocketException();
                fileOutputStream.write(buffer, 0, bytes);
                size -= bytes;
                acc += bytes;

                currProgress.set(total - size);
                final int percentage = (int) ((float) currProgress.get() * 100f / total);

                //Viene calcolata la velocità di trasferimento
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
            file = null;
            updateProgressBar();
            dataInputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            if(currProgressBar != null) {
                if(currProgressBar.getIsCanceled() || currProgressBar.getIsFailed()) {
                    if(file != null)
                        file.delete();
                    return;
                }
                if(currProgressBar.getProgressBar().getValue() < 100) {
                    currProgressBar.setCanceled();
                    if(file != null)
                        file.delete();
                }
            }
        }
    }
    /**
     * Updates the UI of the progressbar panel
     * */
    public void updateProgressBarUI() {
        DownloadPanel.progressBarPanel.revalidate();
        DownloadPanel.progressBarPanel.repaint();
        DownloadPanel.progressBarPanel.updateUI();
        revalidate();
        repaint();
        updateUI();
    }
    /**
     * Loads in all the download panel progress bars
     * */
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
    /**
     * Updates not only the UI but all the states of the progress bars
     * */
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
        //Se il programma è ridotto a icona e non c'è nessun download in corso chiude il programma
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
            try {
                DownloadPanel.sendForgetMeMessage();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.exit(0);
        }
    }
    /**
     * Sends a forgetMe packet in the multicast group
     * */
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
    /**
     * changes the language based on the current language selected
     * */
    public static void changeLanguage() {
        if(searchLabel == null) return;
        if(MainFrame.language.equals("English")) {
            searchLabel.setText("Receive a file");
            deviceName.setText("The device is visible as: " + deviceNameString);
            wifiWarning.setText("This device is now visible to all the devices connected to the same WiFi network.");
            receivedFileError = "Couldn't receive the file: ";
            clearBtn.setText("Clear all");
        }
        if(MainFrame.language.equals("Italian")) {
            searchLabel.setText("Ricevi un file");
            deviceName.setText("Il dispositivo è visibile come: " + deviceNameString);
            wifiWarning.setText("Questo dispositivo è ora visibile a tutti i dispositivi connessi alla tua stessa rete WiFi.");
            receivedFileError = "Non è stato possibile ricevere il file: ";
            clearBtn.setText("Pulisci");
        }
    }
}