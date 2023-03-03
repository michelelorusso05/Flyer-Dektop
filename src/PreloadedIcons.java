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
    public final static ImageIcon icon;
    public final static ImageIcon spinner;

    static {
        phone = new SVGImageLoader(PreloadedIcons.class.getResource("phone.svg").toString(), 72, 72).getImage();
        tablet = new SVGImageLoader(PreloadedIcons.class.getResource("tablet.svg").toString(), 72, 72).getImage();
        windows = new SVGImageLoader(PreloadedIcons.class.getResource("windows.svg").toString(), 72, 72).getImage();
        backArrow = new SVGImageLoader(PreloadedIcons.class.getResource("arrowBack.svg").toString(), 32, 32).getImage();
        wifi = new SVGImageLoader(PreloadedIcons.class.getResource("wifi.svg").toString(), 32, 32).getImage();
        genericFile = new SVGImageLoader(PreloadedIcons.class.getResource("genericFile.svg").toString(), 32, 32).getImage();
        folder = new SVGImageLoader(PreloadedIcons.class.getResource("folder.svg").toString(), 32, 32).getImage();
        icon = new ImageIcon(PreloadedIcons.class.getResource("icon.png"));
        spinner = new ImageIcon(PreloadedIcons.class.getResource("spinner.gif"));
    }
}
