import javax.swing.*;
import java.awt.*;

public class PreloadedIcons {
    public final static Image phone;
    public final static Image tablet;
    public final static Image windows;
    public final static Image backArrow;
    public final static Image wifi;
    public final static Image genericFile;
    public final static Image folder;
    public final static Image icon;
    public final static ImageIcon spinner;
    public final static Image close;
    //Precarico tutte le immagini per evitare di doverle caricare in runtime
    static {
        phone = new SVGImageLoader(PreloadedIcons.class.getResource("phone.svg").toString(), 72, 72).getImage();
        tablet = new SVGImageLoader(PreloadedIcons.class.getResource("tablet.svg").toString(), 72, 72).getImage();
        windows = new SVGImageLoader(PreloadedIcons.class.getResource("windows.svg").toString(), 72, 72).getImage();
        backArrow = new SVGImageLoader(PreloadedIcons.class.getResource("arrowBack.svg").toString(), 32, 32).getImage();
        wifi = new SVGImageLoader(PreloadedIcons.class.getResource("wifi.svg").toString(), 32, 32).getImage();
        genericFile = new SVGImageLoader(PreloadedIcons.class.getResource("genericFile.svg").toString(), 32, 32).getImage();
        folder = new SVGImageLoader(PreloadedIcons.class.getResource("folder.svg").toString(), 32, 32).getImage();
        icon = new SVGImageLoader(PreloadedIcons.class.getResource("icon.svg").toString(), 500, 500).getImage();
        spinner = new ImageIcon(PreloadedIcons.class.getResource("spinner.gif"));
        close = new SVGImageLoader(PreloadedIcons.class.getResource("close.svg").toString(), 8, 8).getImage();
    }
}
