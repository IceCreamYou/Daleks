import java.awt.Graphics;


public abstract class Actor {

	public abstract int getX();
	public abstract int getY();

	public boolean checkCollision(Actor other) {
		return getX() == other.getX() && getY() == other.getY();
	}

	public abstract String getImagePath();
	public void draw(Graphics g) {
		int aw = GamePanel.getActorWidth(), ah = GamePanel.getActorHeight();
		Picture.draw(g, getImagePath(), getX() * aw + 1, getY() * ah + 1, aw - 2, ah - 2);
	}

}
