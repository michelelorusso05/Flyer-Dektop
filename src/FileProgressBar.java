import javax.swing.*;
import java.awt.*;

public class FileProgressBar extends JPanel{
    private JLabel fileName;
    private JProgressBar progressBar;

    public FileProgressBar(String fileName) {
        super(new BorderLayout(5, 5));
        this.fileName = new JLabel(fileName, SwingConstants.CENTER);
        this.progressBar = new JProgressBar();
        add(this.fileName);
        add(this.progressBar, BorderLayout.SOUTH);
    }

    public JProgressBar getProgressBar(){return progressBar;}
}
