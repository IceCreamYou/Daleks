


public class Enemy extends Actor {

	private int x, y;

	public Enemy(int xp, int yp) {
		x = xp;
		y = yp;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public String getImagePath() {
		return "enemy.png";
	}

	public void move(Player player) {
		if (x < player.getX())
			x++;
		else if (x > player.getX())
			x--;
		if (y < player.getY())
			y++;
		else if (y > player.getY())
			y--;
	}

}
