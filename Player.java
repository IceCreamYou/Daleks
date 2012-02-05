import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;


public class Player extends Actor {

	private int x, y;

	private boolean shieldEnabled = false;

	private Map<Item.ItemType, Integer> items = new HashMap<Item.ItemType, Integer>();

	private static final int DEFAULT_BOMBS = 1, DEFAULT_SAFE_TELEPORTS = 1;
	private static final int MAX_ADD_BOMBS = 5, MAX_ADD_SAFE_TELEPORTS = 3;

	private String normalImagePath = "player-2.png";

	public Player(int px, int py) {
		x = px;
		y = py;
		resetItems(1);
		normalImagePath = getRandomPlayerImage();
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
		return shieldEnabled ? "player-shielded.png" : normalImagePath;
	}

	public boolean isShieldEnabled() {
		return shieldEnabled;
	}

	public int getLives() {
		return getItemCount(Item.ItemType.LIFE);
	}

	public int getItemCount(Item.ItemType t) {
		if (items.containsKey(t))
			return items.get(t);
		return 0;
	}

	public void toggleShield() {
		shieldEnabled = !shieldEnabled;
	}

	public void addItem(Item i) {
		Item.ItemType t = i.getType();
		if (items.containsKey(t))
			items.put(t, items.get(t)+1);
		else
			items.put(t, 1);
	}

	public boolean useItem(GamePanel controller, Item.ItemType t) {
		if (items.containsKey(t) && items.get(t) > 0) {
			items.put(t, items.get(t)-1);
			controller.useItem(t);
			return true;
		}
		return false;
	}

	public void resetItems(int level) {
		// Start with the default number of bombs, or add the default number if we already have some.
		if (items.containsKey(Item.ItemType.BOMB) && items.get(Item.ItemType.BOMB) > 0) {
			int amt = items.get(Item.ItemType.BOMB);
			// Don't add too many bombs.
			items.put(Item.ItemType.BOMB, Math.min(amt + DEFAULT_BOMBS, MAX_ADD_BOMBS));
		}
		else
			items.put(Item.ItemType.BOMB, DEFAULT_BOMBS);
		// Start with the default number of teleports, or add the default number if we already have some.
		// The default number of teleports can be higher on higher levels.
		int teleports = DEFAULT_SAFE_TELEPORTS + ((level / GamePanel.EXPAND_LEVEL) * (1 - (level % 2)));
		if (items.containsKey(Item.ItemType.SAFE_TELEPORT) && items.get(Item.ItemType.SAFE_TELEPORT) > 0) {
			int amt = items.get(Item.ItemType.SAFE_TELEPORT);
			// Don't add too many teleports.
			items.put(Item.ItemType.SAFE_TELEPORT, Math.min(amt + teleports, MAX_ADD_SAFE_TELEPORTS));
		}
		else
			items.put(Item.ItemType.SAFE_TELEPORT, teleports);
	}
	public void clearItems() {
		items.clear();
	}

	public void resetPosition() {
		x = GamePanel.getGridWidth() / 2;
		y = GamePanel.getGridHeight() / 2;
	}

	public void move(KeyEvent e) {
		switch(e.getKeyChar()) {
		case '1': y++; x--; break; // DOWN LEFT
		case '2': y++; break; // DOWN
		case '3': y++; x++; break; // DOWN RIGHT
		case '4': x--; break; // LEFT
		case '5': break; // STAY HERE
		case '6': x++; break; // RIGHT
		case '7': y--; x--; break; // UP LEFT
		case '8': y--; break; // UP
		case '9': y--; x++; break; // UP RIGHT
		}
		clip();
	}
	public void move(MouseEvent e) {
		int cx = e.getX() / GamePanel.getActorWidth();
		int cy = e.getY() / GamePanel.getActorHeight();
		if (cx < x)
			x--;
		else if (cx > x)
			x++;
		if (cy < y)
			y--;
		else if (cy > y)
			y++;
		clip();
	}

	private void clip() {
		if (x < 0)
			x = 0;
		else if (x > GamePanel.getGridWidth()-1)
			x = GamePanel.getGridWidth()-1;
		if (y < 0)
			y = 0;
		else if (y > GamePanel.getGridHeight()-1)
			y = GamePanel.getGridHeight()-1;
	}

	public void teleport(Enemies enemies, Items items, Piles piles, boolean safe) {
		// Change clothing every time the player teleports. Hey, we're in an alternate reality.
		normalImagePath = getRandomPlayerImage();

		// Generate random coordinates. If unsafe, we're done.
		x = Utility.getRandomInRange(0, GamePanel.getGridWidth()-1);
		y = Utility.getRandomInRange(0, GamePanel.getGridHeight()-1);
		if (!safe)
			return;

		int count = 0;
		// Make sure the player doesn't spawn on top of items.
		while (items.checkCollision(x, y)) {
			// Don't run infinitely if we run out of room.
			count++;
			if (count > 10000)
				break;
			// Get new coordinates.
			x = Utility.getRandomInRange(0, GamePanel.getGridWidth()-1);
			y = Utility.getRandomInRange(0, GamePanel.getGridHeight()-1);
		}

		// Make sure the player doesn't spawn on top of enemies.
		while (enemies.checkTeleportCollision(x, y)) {
			x = Utility.getRandomInRange(0, GamePanel.getGridWidth()-1);
			y = Utility.getRandomInRange(0, GamePanel.getGridHeight()-1);
		}

		// Make sure the player doesn't spawn on top of piles.
		while (piles.checkCollision(x, y)) {
			x = Utility.getRandomInRange(0, GamePanel.getGridWidth()-1);
			y = Utility.getRandomInRange(0, GamePanel.getGridHeight()-1);
		}
	}

	private static String getRandomPlayerImage() {
		return Utility.getRandomArrayElement(new String[] {
				"player-2.png",
				"player-3.png",
				"player-4.png",
				"player-5.png",
				"player-6.png",
				"player-7.png",
				"player-8.png",
		});
	}

}
