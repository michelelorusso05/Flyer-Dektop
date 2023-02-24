import javax.swing.*;
import java.awt.*;

public class UploadPanel extends JPanel {
    public UploadPanel(CardLayout cardLayout, JPanel cardsPanel) {
        setLayout(new BorderLayout());

        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton backBtn = new JButton();
        backBtn.setIcon(new ImageIcon("./res/backArrow.png"));
        backBtn.addActionListener((e) -> {
            cardLayout.show(cardsPanel, "selection");
        });

        JLabel searchLabel = new JLabel("Ricerca in corso...");
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        northPanel.add(backBtn);
        northPanel.add(searchLabel);

        add(northPanel, BorderLayout.NORTH);
    }
}
