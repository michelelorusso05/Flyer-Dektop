import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class MainFrame extends JFrame {
    public ArrayList<FileProgressBarPanel> uploadProgressBar;
    public ArrayList<FileProgressBarPanel> downloadProgressBar;
    public static String language = "Italian";
    public MainFrame() {
        //Viene impostato il titolo della finestra chiamando il costruttore base della classe JFrame
        super("Flyer");
        //Viene impostato il look and feel di FlatLaf con tema scuro
        FlatDarkLaf.setup();
        //Viene impostata l'icona che appare sulla barra delle applicazioni
        setIconImage(PreloadedIcons.icon.getImage());

        //Viene creato il pulsante sulla barra del menu per poter cambiare la lingua
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

        //Vengono inizializzate gli attributi per le progressbar in upload e in download
        this.uploadProgressBar = new ArrayList<>();
        this.downloadProgressBar = new ArrayList<>();

        //Vengono arrotondati i bordi di tutti i componenti
        UIManager.put( "Button.arc", 15 );
        UIManager.put( "Component.arc", 15 );
        UIManager.put( "ProgressBar.arc", 15 );
        UIManager.put( "TextComponent.arc", 15 );

        //Imposto la dimensione iniziale della finestra a 854x480
        setBounds(0, 0, 854, 480);
        //Imposto la dimensione minima che la finestra può avere a 854x480
        setMinimumSize(new Dimension(854, 480));
        //Centro la finestra sullo schermo
        setLocationRelativeTo(null);
        //Aggiungo il window listener per gestire la chiusura dell'applicazione
        addWindowListener(new WindowEventManager(this));

        //Creo il card layout per poter gestire tutti i panel
        CardLayout cardLayout = new CardLayout();
        //Creo il cards panel dentro cui aggiungerò tutti i panel che rappresentano le diverse schermate
        //e nel costruttore passo il card layout
        JPanel cards = new JPanel(cardLayout);

        //Creo il panel iniziale per la selezione dell'azione da effettuare
        ActionSelectionPanel actionSelectionPanel = new ActionSelectionPanel(cardLayout, cards, this);

        //Aggiungo al cards panel il panel per la selezione dell'azione e per poterlo in seguito
        //visualizzare assegno a questo panel il nome "selection"
        cards.add(actionSelectionPanel, "selection");

        //Tolgo l'operazione default in chiusura
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //Imposto come content pane del frame il cards panel
        getContentPane().add(cards);
        //Rendo visibile il frame
        setVisible(true);
    }
}