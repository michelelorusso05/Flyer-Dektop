import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.InetAddress;

public class FileProgressBarPanel extends JPanel{
    private final JLabel fileName;
    private final JProgressBar progressBar;
    private final InetAddress ip;
    private boolean isClosed = false;
    private boolean needUpdate = false;

    public FileProgressBarPanel(String fileName, InetAddress ip) {
        super(new BorderLayout(5, 5));
        this.fileName = new JLabel(fileName, SwingConstants.CENTER);
        this.progressBar = new JProgressBar();
        this.ip = ip;
        add(this.fileName);

        JPanel southPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton();
        closeButton.addActionListener(e -> {
            if(this.progressBar.getValue() == 100) {
                isClosed = true;
                needUpdate = true;
            }
        });
        closeButton.setIcon(new ImageIcon(PreloadedIcons.close));
        southPanel.add(this.progressBar, BorderLayout.SOUTH);
        southPanel.add(closeButton);
        add(southPanel, BorderLayout.SOUTH);
    }
    public JProgressBar getProgressBar(){return progressBar;}
    public String getName() {return fileName.getText();}
    public InetAddress getIp() {return ip;}
    public boolean getIsClosed() {return isClosed;}
    public boolean getNeedUpdate() {return this.needUpdate;}
    public void setNeedUpdate(boolean needUpdate) {this.needUpdate = needUpdate;}
}
