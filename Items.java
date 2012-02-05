import java.awt.Graphics;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class Items {

	Set<Item> items = new HashSet<Item>();

	public Items(int level, Player player, Enemies e) {
		// Only add items in every other level.
		if (level % 2 == 0)
			return;
		// Create as many items as the level requires.
		for (int i = 0; i < 1 + (level * 0.12) * (level % 3) + (level / GamePanel.EXPAND_LEVEL) * 2; i++) {
			// Make sure items don't spawn in the same place as the player.
			int x = Utility.getRandomInRange(0, GamePanel.getGridWidth()-1);
			int y = Utility.getRandomInRange(0, GamePanel.getGridHeight()-1);
			while (x == player.getX() && y == player.getY()) {
				x = Utility.getRandomInRange(0, GamePanel.getGridWidth()-1);
				y = Utility.getRandomInRange(0, GamePanel.getGridHeight()-1);
			}

			// Make sure items don't spawn in the same place as another item.
			int count = 0;
			boolean collision = true;
			while (collision) {
				// Don't run infinitely if we run out of room.
				count++;
				if (count >= 10000)
					break;
				// Check if other enemies are at the same location.
				collision = false;
				for (Item item : items) {
					if (x == item.getX() && y == item.getY()) {
						x = Utility.getRandomInRange(0, GamePanel.getGridWidth()-1);
						y = Utility.getRandomInRange(0, GamePanel.getGridHeight()-1);
						collision = true;
						break;
					}
				}
			}

			// Make sure items don't spawn in the same place as any enemies.
			count = 0;
			while (e.checkCollision(x, y)) {
				// Don't run infinitely if we run out of room.
				count++;
				if (count >= 100000)
					break;
				// Get new coordinates.
				x = Utility.getRandomInRange(0, GamePanel.getGridWidth()-1);
				y = Utility.getRandomInRange(0, GamePanel.getGridHeight()-1);
			}

			// If we have no collisions, add the item to our list.
			items.add(new Item(x, y, Item.ItemType.getRandomType()));
		}
	}

	public void processCollision(Enemy e) {
		Iterator<Item> it = items.iterator();
		while (it.hasNext()) {
			Item i = it.next();
			if (i.checkCollision(e))
				it.remove();
		}
	}
	public void processCollision(Player p, GamePanel controller) {
		Iterator<Item> it = items.iterator();
		while (it.hasNext()) {
			Item i = it.next();
			if (i.checkCollision(p)) {
				p.addItem(i);
				// Activate the shield immediately.
				if (i.getType() == Item.ItemType.SHIELD)
					p.useItem(controller, Item.ItemType.SHIELD);
				it.remove();
			}
		}
	}
	boolean checkCollision(int x, int y) {
		for (Item i : items)
			if (i.getX() == x && i.getY() == y)
				return true;
		return false;
	}

	public void bomb(Player player) {
		Iterator<Item> it = items.iterator();
		while (it.hasNext()) {
			Item i = it.next();
			if (Utility.within(i.getX(), 1, player.getX()) && Utility.within(i.getY(), 1, player.getY())) {
				it.remove();
			}
		}
	}

	public void draw(Graphics g) {
		for (Item i : items)
			i.draw(g);
	}

}
