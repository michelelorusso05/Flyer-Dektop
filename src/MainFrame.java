import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame() throws HeadlessException {
        super("Flyer");
        FlatDarkLaf.setup();

        UIManager.put( "Button.arc", 15 );
        UIManager.put( "Component.arc", 15 );
        UIManager.put( "ProgressBar.arc", 15 );
        UIManager.put( "TextComponent.arc", 15 );

        setBounds(0, 0, 854, 480);
        setMinimumSize(new Dimension(854, 480));
        setLocationRelativeTo(null);
        addWindowListener(new WindowEventManager());

        // Setup manager
        CardLayout cardLayout = new CardLayout();
        JPanel cards = new JPanel(cardLayout);

        ActionSelectionPanel actionSelectionPanel = new ActionSelectionPanel(cardLayout, cards);

        cards.add(actionSelectionPanel, "selection");

        // Set listeners
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Finalize
        getContentPane().add(cards);
        setVisible(true);
    }
}
