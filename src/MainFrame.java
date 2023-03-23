import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class MainFrame extends JFrame {
    public ArrayList<FileProgressBarPanel> uploadProgressBar;
    public ArrayList<FileProgressBarPanel> downloadProgressBar;
    public static String language = "Italian";
    public MainFrame() throws HeadlessException {
        super("Flyer");
        FlatDarkLaf.setup();
        setIconImage(PreloadedIcons.icon.getImage());

        JMenuBar menuBar = new JMenuBar();
        JMenu languageMenu = new JMenu("Lingua");
        JMenuItem italianItem = new JMenuItem("Italiano");
        italianItem.addActionListener(e -> {
            languageMenu.setText("Lingua");
            MainFrame.language = "Italian";
            UploadPanel.changeLanguage();
            DownloadPanel.changeLanguage();
            ActionSelectionPanel.changeLanguage();
            for(FileProgressBarPanel el : uploadProgressBar) {
                el.changeLanguage();
            }
            for(FileProgressBarPanel el : downloadProgressBar) {
                el.changeLanguage();
            }
        });
        JMenuItem englishItem = new JMenuItem("English");
        englishItem.addActionListener(e -> {
            languageMenu.setText("Language");
            MainFrame.language = "English";
            UploadPanel.changeLanguage();
            DownloadPanel.changeLanguage();
            ActionSelectionPanel.changeLanguage();
            for(FileProgressBarPanel el : uploadProgressBar) {
                el.changeLanguage();
            }
            for(FileProgressBarPanel el : downloadProgressBar) {
                el.changeLanguage();
            }
        });
        languageMenu.add(italianItem);
        languageMenu.add(englishItem);
        menuBar.add(languageMenu);
        setJMenuBar(menuBar);

        this.uploadProgressBar = new ArrayList<>();
        this.downloadProgressBar = new ArrayList<>();

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

        cards.add(actionSelectionPanel, "selection");

        // Finalize
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        getContentPane().add(cards);
        setVisible(true);
    }
}