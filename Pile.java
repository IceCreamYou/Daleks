


public class Pile extends Actor {

	private int x, y;

	public Pile(int px, int py) {
		x = px;
		y = py;
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
		return "pile.png";
	}

}
