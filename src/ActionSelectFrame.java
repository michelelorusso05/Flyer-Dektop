import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentListener;

public class ActionSelectFrame extends JFrame {
    public ActionSelectFrame() throws HeadlessException {
        super("Flyer");
        FlatDarkLaf.setup();

        UIManager.put( "Button.arc", 50 );
        UIManager.put( "Component.arc", 50 );
        UIManager.put( "ProgressBar.arc", 50 );
        UIManager.put( "TextComponent.arc", 50 );

        setBounds(0, 0, 800, 600);
        setLocationRelativeTo(null);
        addWindowListener(new WindowEventManager());

        // Setup manager
        CardLayout cardLayout = new CardLayout();
        JPanel cards = new JPanel(cardLayout);

        ActionSelectionPanel actionSelectionPanel = new ActionSelectionPanel("", cardLayout, cards);
        ActionSelectionPanel actionSelectionPanel2 = new ActionSelectionPanel("siummete", cardLayout, cards);

        cards.add(actionSelectionPanel, "0");
        cards.add(actionSelectionPanel2, "1");

        // Set listeners
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Finalize
        getContentPane().add(cards);
        setVisible(true);
    }
}
