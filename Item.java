


public class Item extends Actor {

	public enum ItemType {
		SAFE_TELEPORT, BOMB, SHIELD, LIFE, PILE;

		public static ItemType getRandomType() {
			double r = Math.random();
			if (r < 0.10)
				return LIFE;		// 10%
			else if (r < 0.25)
				return PILE;		// 15%
			else if (r < 0.40)
				return SHIELD;		// 15%
			else if (r < 0.65)
				return BOMB;		// 25%
			return SAFE_TELEPORT;	// 35%
		}
	}

	private int x, y;

	private ItemType type;

	public Item(int px, int py, ItemType t) {
		if (t == null)
			throw new RuntimeException("Item type should not be null");
		x = px;
		y = py;
		type = t;
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
		switch (type) {
		case BOMB: return "bomb.png";
		case LIFE: return "life.png";
		case PILE: return "pile-item.png";
		case SAFE_TELEPORT: return "teleporter.png";
		case SHIELD: default: return "shield.png";
		}
	}

	public ItemType getType() {
		return type;
	}

}
