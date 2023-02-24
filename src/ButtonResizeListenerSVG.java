import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class ButtonResizeListenerSVG extends ButtonResizeListener {
    private final String path;
    public ButtonResizeListenerSVG(String path) {
        super(path);
        this.path = path;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        Dimension newSize = e.getComponent().getBounds().getSize();

        float size = (float)(Math.min(newSize.getWidth(), newSize.getHeight()));

        JButton btn = (JButton) e.getComponent();
        Image image = new SVGImageLoader(this.path, size - (size / 2), size - (size / 2)).getImage();
        btn.setIcon(new ImageIcon(image));
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }
}
