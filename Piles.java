import java.awt.Graphics;
import java.util.HashSet;
import java.util.Set;


public class Piles {

	private Set<Pile> piles;

	public Piles() {
		piles = new HashSet<Pile>();
	}

	public boolean checkCollision(Actor a) {
		for (Pile p : piles)
			if (p.checkCollision(a))
				return true;
		return false;
	}
	boolean checkCollision(int x, int y) {
		for (Pile p : piles)
			if (p.getX() == x && p.getY() == y)
				return true;
		return false;
	}

	public void add(int x, int y) {
		if (!checkCollision(x, y)) // Don't add a pile where one already exists.
			piles.add(new Pile(x, y));
	}

	public void draw(Graphics g) {
		for (Pile p : piles)
			p.draw(g);
	}

}
