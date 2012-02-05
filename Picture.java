import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Provides methods to facilitate drawing images.
 */
public class Picture {
	
	/**
	 * Keep track of pictures that have already been drawn so that we don't have to load them every time.
	 */
	private static Map<String, BufferedImage> cache = new HashMap<String, BufferedImage>();

	/**
	 * Draw an image.
	 *
	 * @param g The graphics context in which to draw the image.
	 * @param filepath The location of the image file.
	 * @param x The x-coordinate of where the upper-left corner of the image should be drawn.
	 * @param y The y-coordinate of where the upper-left corner of the image should be drawn.
	 * @param w The width at which the image should be drawn.
	 * @param h The height at which the image should be drawn.
	 */
	public static void draw(Graphics g, String filepath, int x, int y, int w, int h) {
		try {
			BufferedImage img;
			if (cache.containsKey(filepath))
				img = cache.get(filepath);
			else {
				img = ImageIO.read(Picture.class.getResource("images/"+ filepath));
				cache.put(filepath, img);
			}
			// This isn't super fast with lots of images on the screen.
			g.drawImage(img.getScaledInstance(w, h, Image.SCALE_SMOOTH), x, y, null);
		} catch (IOException e) {
			System.err.println(e.getMessage() +" "+ filepath);
		}
	}

}
