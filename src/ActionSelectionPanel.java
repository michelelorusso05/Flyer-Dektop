import javax.swing.*;
import java.awt.*;

public class ActionSelectionPanel extends JPanel {
    public ActionSelectionPanel(CardLayout cardLayout, JPanel cardsPanel) {
        SpringLayout layout = new SpringLayout();
        setLayout(layout);

        // Add components
        JButton upload = new JButton();
        upload.addComponentListener(new ButtonResizeListenerSVG("./res/upload.svg"));
        upload.addActionListener((e) -> {
            cardLayout.show(cardsPanel, "upload");
        });

        JButton download = new JButton();
        download.addComponentListener(new ButtonResizeListenerSVG("./res/download.svg"));

        add(upload);
        add(download);

        // Set constraints
        layout.putConstraint(SpringLayout.WEST, upload, 16, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, upload, 16, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.EAST, upload, -16, SpringLayout.HORIZONTAL_CENTER, this);
        layout.putConstraint(SpringLayout.SOUTH, upload, -16, SpringLayout.SOUTH, this);

        layout.putConstraint(SpringLayout.WEST, download, 16, SpringLayout.HORIZONTAL_CENTER, this);
        layout.putConstraint(SpringLayout.NORTH, download, 16, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.EAST, download, -16, SpringLayout.EAST, this);
        layout.putConstraint(SpringLayout.SOUTH, download, -16, SpringLayout.SOUTH, this);
    }
}
