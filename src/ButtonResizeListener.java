import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class ButtonResizeListener implements ComponentListener {
    protected final ImageIcon icon;

    public ButtonResizeListener(String path) {
        this.icon = new ImageIcon(path);
    }
    @Override
    public void componentResized(ComponentEvent e) {
        Dimension newSize = e.getComponent().getBounds().getSize();

        int size = (int)(Math.min(newSize.getWidth(), newSize.getHeight()));

        JButton btn = (JButton) e.getComponent();
        Image image = this.icon.getImage();
        Image imageScaled = image.getScaledInstance(size - (size / 2), size - (size / 2),  java.awt.Image.SCALE_SMOOTH);
        btn.setIcon(new ImageIcon(imageScaled));
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
