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
    public final static ImageIcon spinner;

    static {
        phone = new SVGImageLoader("./res/phone.svg", 72, 72).getImage();
        tablet = new SVGImageLoader("./res/tablet.svg", 72, 72).getImage();
        windows = new SVGImageLoader("./res/windows.svg", 72, 72).getImage();
        backArrow = new SVGImageLoader("./res/arrowBack.svg", 32, 32).getImage();
        wifi = new SVGImageLoader("./res/wifi.svg", 32, 32).getImage();
        genericFile = new SVGImageLoader("./res/genericFile.svg", 32, 32).getImage();
        folder = new SVGImageLoader("./res/folder.svg", 32, 32).getImage();
        spinner = new ImageIcon("./res/spinner.gif");
    }
}
