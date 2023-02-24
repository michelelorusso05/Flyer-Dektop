import java.awt.*;

public class PreloadedIcons {
    public final static Image phone;
    public final static Image backArrow;
    public final static Image wifi;

    static {
        backArrow = new SVGImageLoader("./res/arrowBack.svg", 32, 32).getImage();
        wifi = new SVGImageLoader("./res/wifi.svg", 32, 32).getImage();
        phone = new SVGImageLoader("./res/phone.svg", 72, 72).getImage();
    }
}
