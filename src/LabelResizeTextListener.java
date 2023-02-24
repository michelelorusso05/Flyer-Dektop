import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class LabelResizeTextListener implements ComponentListener {
    private final JLabel label;

    public LabelResizeTextListener(JLabel label) {
        this.label = label;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        Dimension newSize = e.getComponent().getBounds().getSize();

        int size = (int)(Math.min(newSize.getWidth(), newSize.getHeight()));
        this.label.setFont(new Font("Arial", Font.PLAIN, size / 6));
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
