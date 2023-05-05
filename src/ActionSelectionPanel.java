import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.*;
import java.util.ArrayList;

public class ActionSelectionPanel extends JPanel {
    private File selectedFile;
    private File selectedDirectory;
    private final ActionSelectionPanel actionSelectionPanel;

    private static JLabel uploadLabel;
    private static JLabel downloadLabel;
    private static String uploadFileChooserText = "Selezione il file da inviare";
    private static String downloadDirectoryChooserText = "Seleziona la cartella dove salvare il file";

    public ActionSelectionPanel(CardLayout cardLayout, JPanel cardsPanel, MainFrame mainFrame) {
        SpringLayout layout = new SpringLayout();
        setLayout(layout);
        actionSelectionPanel = this;

        JButton upload = new JButton();
        JPanel innerUploadBtnPanel = new JPanel(new BorderLayout());
        innerUploadBtnPanel.setOpaque(true);
        innerUploadBtnPanel.setBackground(new Color(0,0,0,0));

        JLabel uploadIcon = new JLabel("", SwingConstants.CENTER);
        upload.addComponentListener(new LabelResizeListener(this.getClass().getResource("upload.svg").toString(), uploadIcon));
        innerUploadBtnPanel.add(uploadIcon, BorderLayout.CENTER);

        uploadLabel = new JLabel("Invia", SwingConstants.CENTER);
        upload.addComponentListener(new LabelResizeTextListener(uploadLabel, 6));
        innerUploadBtnPanel.add(uploadLabel, BorderLayout.SOUTH);

        upload.add(innerUploadBtnPanel);

        //Apre una finestra di esplora file per far scegliere un file
        upload.addActionListener((e) -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(uploadFileChooserText);
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                this.selectedFile = fileChooser.getSelectedFile();
                UploadPanel uploadPanel = new UploadPanel(cardLayout, cardsPanel, this, mainFrame);
                cardsPanel.add(uploadPanel, "upload");
                cardLayout.show(cardsPanel, "upload");
            }
        });

        //Download Button
        JButton download = new JButton();
        JPanel innerDownloadBtnPanel = new JPanel(new BorderLayout());
        innerDownloadBtnPanel.setOpaque(true);
        innerDownloadBtnPanel.setBackground(new Color(0,0,0,0));

        JLabel downloadIcon = new JLabel("", SwingConstants.CENTER);
        download.addComponentListener(new LabelResizeListener(this.getClass().getResource("download.svg").toString(), downloadIcon));
        innerDownloadBtnPanel.add(downloadIcon, BorderLayout.CENTER);

        downloadLabel = new JLabel("Ricevi", SwingConstants.CENTER);
        download.addComponentListener(new LabelResizeTextListener(downloadLabel, 6));
        innerDownloadBtnPanel.add(downloadLabel, BorderLayout.SOUTH);

        download.add(innerDownloadBtnPanel);

        //Apre una finestra di esplora file per far scegliere una cartella
        download.addActionListener((e) -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(downloadDirectoryChooserText);
            chooser.setCurrentDirectory(new java.io.File("."));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                this.selectedDirectory = chooser.getSelectedFile();
                DownloadPanel downloadPanel = new DownloadPanel(cardLayout, cardsPanel, this, mainFrame);
                cardsPanel.add(downloadPanel, "download");
                cardLayout.show(cardsPanel, "download");
            }
        });

        //add buttons to panel
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

        setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    java.util.List<File> droppedFiles = (java.util.List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    selectedFile = droppedFiles.get(0);
                    if(selectedFile.isDirectory()) return;
                    UploadPanel uploadPanel = new UploadPanel(cardLayout, cardsPanel, actionSelectionPanel, mainFrame);
                    cardsPanel.add(uploadPanel, "upload");
                    cardLayout.show(cardsPanel, "upload");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    /**
     * returns the file selected by the user
     * @return File file
     * */
    public File getSelectedFile() {return selectedFile;}
    /**
     * returns the directory selected by the user
     * @return File directory
     * */
    public File getSelectedDirectory() {return selectedDirectory;}

    /**
     * changes the language based on the current language selected
     * */
    public static void changeLanguage() {
        if(uploadLabel == null) return;
        if(MainFrame.language.equals("English")) {
            uploadLabel.setText("Send");
            downloadLabel.setText("Receive");
            uploadFileChooserText = "Select the file to send";
            downloadDirectoryChooserText = "Select the folder where to save the file";
        }
        if(MainFrame.language.equals("Italian")) {
            uploadLabel.setText("Invia");
            downloadLabel.setText("Ricevi");
            uploadFileChooserText = "Selezione il file da inviare";
            downloadDirectoryChooserText = "Seleziona la cartella dove salvare il file";
        }
    }
}
