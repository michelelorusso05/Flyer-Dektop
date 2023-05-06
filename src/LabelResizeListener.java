import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class LabelResizeListener implements ComponentListener {

    private final String path;
    private final JLabel label;

    public LabelResizeListener(String path, JLabel label) {
        this.path = path;
        this.label = label;
    }
    /**
     * This function is called when the container component is resized
     * @param e component event
     * */
    @Override
    public void componentResized(ComponentEvent e) {
        Dimension newSize = e.getComponent().getBounds().getSize();
        float size = (float)(Math.min(newSize.getWidth(), newSize.getHeight()));
        Image image = new SVGImageLoader(this.path, size - (size / 2), size - (size / 2)).getImage();
        this.label.setIcon(new ImageIcon(image));
        e.getComponent().invalidate();
        e.getComponent().repaint();
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
