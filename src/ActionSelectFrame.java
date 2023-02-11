import javax.swing.*;
import java.awt.*;

public class ActionSelectFrame extends JFrame {
    public ActionSelectFrame() throws HeadlessException {
        super("Flyer");
        setBounds(0, 0, 800, 600);
        setLocationRelativeTo(null);

        // Setup manager
        JPanel main = new JPanel();
        SpringLayout layout = new SpringLayout();
        main.setLayout(layout);

        // Add components
        JButton upload = new JButton();
        upload.setIcon(new ImageIcon("./res/upload.png"));
        JButton download = new JButton();
        download.setIcon(new ImageIcon("./res/download.png"));
        main.add(upload);
        main.add(download);

        // Set constraints
        layout.putConstraint(SpringLayout.WEST, upload, 16, SpringLayout.WEST, main);
        layout.putConstraint(SpringLayout.NORTH, upload, 16, SpringLayout.NORTH, main);
        layout.putConstraint(SpringLayout.EAST, upload, -16, SpringLayout.HORIZONTAL_CENTER, main);
        layout.putConstraint(SpringLayout.SOUTH, upload, -16, SpringLayout.SOUTH, main);

        layout.putConstraint(SpringLayout.WEST, download, 16, SpringLayout.HORIZONTAL_CENTER, main);
        layout.putConstraint(SpringLayout.NORTH, download, 16, SpringLayout.NORTH, main);
        layout.putConstraint(SpringLayout.EAST, download, -16, SpringLayout.EAST, main);
        layout.putConstraint(SpringLayout.SOUTH, download, -16, SpringLayout.SOUTH, main);

        // Set listeners
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Finalize
        getContentPane().add(main);
        setVisible(true);
    }
}
