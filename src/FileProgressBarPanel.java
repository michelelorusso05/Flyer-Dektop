import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class FileProgressBarPanel extends JPanel{
    private final JLabel fileName;
    private final String actualFileName;
    private final JProgressBar progressBar;
    private final InetAddress ip;
    private boolean isClosed = false;
    private boolean needUpdate = false;
    private final JLabel completed;
    private final JLabel failed;
    private final JLabel canceled;
    private final JLabel transferSpeed = new JLabel("0kB/s");
    private boolean isFailed = false;
    private boolean isCanceled = false;
    private boolean isCompleted = false;

    public FileProgressBarPanel(String path, String fileName, InetAddress ip, Socket socket, boolean isDownload) {
        super(new BorderLayout(5, 5));
        if(isDownload)
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                Desktop desktop = Desktop.getDesktop();
                if(isDownload && isCompleted && !isFailed && !isCanceled) {
                    try {
                        desktop.open(new File(path + File.separator + actualFileName));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        this.actualFileName = fileName;
        JPanel centerPanel = new JPanel(new WrapLayout(FlowLayout.CENTER));
        String currFileName = fileName;
        if(currFileName.length() >= 14) {
            currFileName = currFileName.substring(0, 14) + "…";
        }
        this.fileName = new JLabel(currFileName, SwingConstants.CENTER);
        this.progressBar = new JProgressBar();
        this.ip = ip;
        centerPanel.add(this.fileName);
        centerPanel.add(transferSpeed);
        add(centerPanel);

        this.completed = new JLabel("Operazione completata");
        this.completed.setVisible(false);

        this.failed = new JLabel("Operazione non riuscita");
        this.failed.setVisible(false);

        this.canceled = new JLabel("Operazione annullata");
        this.canceled.setVisible(false);

        JPanel southPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton();
        closeButton.addActionListener(e -> {
            if(this.progressBar.getValue() == 100 || this.isFailed || this.isCanceled) {
                isClosed = true;
                needUpdate = true;
            }else {
                try {
                    setCanceled();
                    socket.close();
                    needUpdate = true;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        closeButton.setIcon(new ImageIcon(PreloadedIcons.close));
        southPanel.add(this.progressBar);
        southPanel.add(this.completed);
        southPanel.add(this.failed);
        southPanel.add(this.canceled);
        southPanel.add(closeButton);
        add(southPanel, BorderLayout.SOUTH);

        changeLanguage();
    }
    public JProgressBar getProgressBar(){return progressBar;}
    public String getName() {return actualFileName;}
    public InetAddress getIp() {return ip;}
    public boolean getIsClosed() {return isClosed;}
    public boolean getNeedUpdate() {return this.needUpdate;}
    public void setNeedUpdate(boolean needUpdate) {this.needUpdate = needUpdate;}
    public void setCompleted() {
        this.progressBar.setVisible(false);
        this.transferSpeed.setVisible(false);
        this.completed.setVisible(true);
        this.isCompleted = true;
    }
    public void setFailed() {
        this.progressBar.setVisible(false);
        this.transferSpeed.setVisible(false);
        this.failed.setVisible(true);
        this.isFailed = true;
    }
    public void setCanceled() {
        this.progressBar.setVisible(false);
        this.transferSpeed.setVisible(false);
        this.canceled.setVisible(true);
        this.isCanceled = true;
    }
    public boolean getIsFailed(){return isFailed;}
    public boolean getIsCanceled(){return isCanceled;}
    public boolean getIsCompleted() {return isCompleted;}
    public void setTransferSpeed(String speed) {this.transferSpeed.setText(speed);}
    public  void changeLanguage() {
        if(MainFrame.language.equals("English")) {
            this.completed.setText("Operation completed");
            this.failed.setText("Operation failed");
            this.canceled.setText("Operation cancelled");
        }
        if(MainFrame.language.equals("Italian")) {
            this.completed.setText("Operazione completata");
            this.failed.setText("Operazione non riuscita");
            this.canceled.setText("Operazione annullata");
        }
    }
}
