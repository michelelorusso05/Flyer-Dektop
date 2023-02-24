import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.DOMImplementation;

import java.awt.image.BufferedImage;

public class SVGImageLoader extends ImageTranscoder {
    private BufferedImage image = null;
    private String path;
    private float width;
    private float height;
    public SVGImageLoader(String path, float w, float h) {
        this.path = path;
        this.width = w;
        this.height = h;
    }

    public BufferedImage createImage(int w, int h) {
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        return image;
    }
    public void writeImage(BufferedImage img, TranscoderOutput out) {
    }
    public BufferedImage getImage() {
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        TranscodingHints hints = new TranscodingHints();
        hints.put(ImageTranscoder.KEY_WIDTH, this.width);
        hints.put(ImageTranscoder.KEY_HEIGHT, this.height);
        hints.put(ImageTranscoder.KEY_DOM_IMPLEMENTATION, impl);
        hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, SVGConstants.SVG_NAMESPACE_URI);
        hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT, SVGConstants.SVG_SVG_TAG);
        hints.put(ImageTranscoder.KEY_XML_PARSER_VALIDATING, false);
        setTranscodingHints(hints);
        try {
            transcode(new TranscoderInput(path), null);
        } catch (TranscoderException e) {
            throw new RuntimeException(e);
        }
        return image;
    }
}
