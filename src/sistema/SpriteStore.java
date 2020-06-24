package sistema;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class SpriteStore {
    /** The single instance of this class **/
    private static SpriteStore single = new SpriteStore();

    private HashMap sprites  = new HashMap();

    public static SpriteStore get() {
        return single;
    }

    public Sprite getSprite(String ref) {

        // If we've already got the sprite in the cache then just return the existing version
        if (sprites.get(ref) != null) {
            return (Sprite) sprites.get(ref);
        }

        // otherwise, go away and grab the sprite frm the resource loader
        BufferedImage sourceImage = null;

        try {
            // The ClassLoader.getResource() ensures we get the sprite from the appropriate place,
            // this helps with deploying the game with things like webstart.
            // You could equally do a file look up here
            URL url = this.getClass().getClassLoader().getResource(ref);

            if (url == null) {
                fail("Can't find ref: " + ref);
            }

            //use ImageIO to read the image in
            sourceImage = ImageIO.read(url);

        } catch (IOException e) {
            fail("Failed to load: " + ref);
        }

        // create an accelerated image of the right size to store our sprite in
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                .getDefaultScreenDevice()
                                                .getDefaultConfiguration();

        Image image = gc.createCompatibleImage(
                sourceImage.getWidth(),
                sourceImage.getHeight(),
                Transparency.BITMASK
        );

        // draw our source image into the accelerated image
        image.getGraphics().drawImage(sourceImage, 0, 0, null);

        // create a sprite, add it the cache then return it
        Sprite sprite = new Sprite(image);
        sprites.put(ref, sprite);

        return sprite;

    }

    private void fail(String message) {
        System.err.println(message);
        System.exit(0);
    }
}
