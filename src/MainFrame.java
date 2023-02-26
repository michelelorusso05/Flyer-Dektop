import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class MainFrame extends JFrame {
    public ArrayList<FileProgressBarPanel> uploadProgressBar;
    public MainFrame() throws HeadlessException {
        super("Flyer");
        FlatDarkLaf.setup();

        this.uploadProgressBar = new ArrayList<>();

        UIManager.put( "Button.arc", 15 );
        UIManager.put( "Component.arc", 15 );
        UIManager.put( "ProgressBar.arc", 15 );
        UIManager.put( "TextComponent.arc", 15 );

        setBounds(0, 0, 854, 480);
        setMinimumSize(new Dimension(854, 480));
        setLocationRelativeTo(null);
        addWindowListener(new WindowEventManager(this));

        // Setup manager
        CardLayout cardLayout = new CardLayout();
        JPanel cards = new JPanel(cardLayout);

        ActionSelectionPanel actionSelectionPanel = new ActionSelectionPanel(cardLayout, cards, this);
        DownloadPanel downloadPanel = new DownloadPanel(cardLayout, cards, actionSelectionPanel, this);

        cards.add(actionSelectionPanel, "selection");
        cards.add(downloadPanel, "download");

        // Finalize
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        getContentPane().add(cards);
        setVisible(true);
    }
}
