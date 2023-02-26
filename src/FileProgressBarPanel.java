import javax.swing.*;
import java.awt.*;

public class FileProgressBarPanel extends JPanel{
    private final JLabel fileName;
    private final JProgressBar progressBar;

    public FileProgressBarPanel(String fileName) {
        super(new BorderLayout(5, 5));
        this.fileName = new JLabel(fileName, SwingConstants.CENTER);
        this.progressBar = new JProgressBar();
        add(this.fileName);
        add(this.progressBar, BorderLayout.SOUTH);
    }

    public JProgressBar getProgressBar(){return progressBar;}
    public String getName() {return fileName.getText();}
}
