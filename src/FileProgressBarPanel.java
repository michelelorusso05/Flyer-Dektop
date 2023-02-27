import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;

public class FileProgressBarPanel extends JPanel{
    private final JLabel fileName;
    private final JProgressBar progressBar;
    private final InetAddress ip;

    public FileProgressBarPanel(String fileName, InetAddress ip) {
        super(new BorderLayout(5, 5));
        this.fileName = new JLabel(fileName, SwingConstants.CENTER);
        this.progressBar = new JProgressBar();
        this.ip = ip;
        add(this.fileName);
        add(this.progressBar, BorderLayout.SOUTH);
    }
    public JProgressBar getProgressBar(){return progressBar;}
    public String getName() {return fileName.getText();}
    public InetAddress getIp() {return ip;}
}
