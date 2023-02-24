import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.keys.LengthKey;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.svg.SVGImageElement;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class UploadPanel extends JPanel {
    public UploadPanel(CardLayout cardLayout, JPanel cardsPanel) {
        setLayout(new BorderLayout());

        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton backBtn = new JButton();

        MyTranscoder transcoder = new MyTranscoder();
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        TranscodingHints hints = new TranscodingHints();
        hints.put(ImageTranscoder.KEY_WIDTH, 32f);
        hints.put(ImageTranscoder.KEY_HEIGHT, 32f);
        hints.put(ImageTranscoder.KEY_DOM_IMPLEMENTATION, impl);
        hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, SVGConstants.SVG_NAMESPACE_URI);
        hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI,SVGConstants.SVG_NAMESPACE_URI);
        hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT, SVGConstants.SVG_SVG_TAG);
        hints.put(ImageTranscoder.KEY_XML_PARSER_VALIDATING, false);
        transcoder.setTranscodingHints(hints);
        try {
            transcoder.transcode(new TranscoderInput("./res/arrowBack.svg"), null);
        } catch (TranscoderException e) {
            throw new RuntimeException(e);
        }
        BufferedImage image = transcoder.getImage();

        backBtn.setIcon(new ImageIcon(image));
        backBtn.addActionListener((e) -> {
            cardLayout.show(cardsPanel, "selection");
        });

        JLabel searchLabel = new JLabel("Ricerca in corso...");
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        northPanel.add(backBtn);
        northPanel.add(searchLabel);

        add(northPanel, BorderLayout.NORTH);
    }
}

class MyTranscoder extends ImageTranscoder {
    private BufferedImage image = null;
    public BufferedImage createImage(int w, int h) {
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        return image;
    }
    public void writeImage(BufferedImage img, TranscoderOutput out) {
    }
    public BufferedImage getImage() {
        return image;
    }
}