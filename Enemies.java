import java.awt.Graphics;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class Enemies {

	private Set<Enemy> enemies = new HashSet<Enemy>();

	public Enemies(int level, Player player) {
		// Create as many enemies as the level requires.
		for (int i = 0; i < 4 + level * 4 + (level / GamePanel.EXPAND_LEVEL); i++) {
			// Make sure enemies don't spawn in the same place as the player.
			int x = Utility.getRandomInRange(0, GamePanel.getGridWidth()-1);
			int y = Utility.getRandomInRange(0, GamePanel.getGridHeight()-1);
			while (x == player.getX() && y == player.getY()) {
				x = Utility.getRandomInRange(0, GamePanel.getGridWidth()-1);
				y = Utility.getRandomInRange(0, GamePanel.getGridHeight()-1);
			}

			// Make sure enemies don't spawn in the same place as another enemy.
			int count = 0;
			boolean collision = true;
			while (collision) {
				// Don't run infinitely if we run out of room.
				count++;
				if (count >= 100000)
					break;
				// Check if other enemies are at the same location.
				collision = false;
				for (Enemy e : enemies) {
					if (x == e.getX() && y == e.getY()) {
						x = Utility.getRandomInRange(0, GamePanel.getGridWidth()-1);
						y = Utility.getRandomInRange(0, GamePanel.getGridHeight()-1);
						collision = true;
						break;
					}
				}
			}

			// If we have no collisions, add the enemy to our list.
			enemies.add(new Enemy(x, y));
		}
	}

	public int getSize() {
		return enemies.size();
	}

	public boolean processCollision(Daleks controller, Player player) {
		Iterator<Enemy> it = enemies.iterator();
		boolean collision = false;
		while (it.hasNext()) {
			Enemy e = it.next();
			if (e.checkCollision(player)) {
				if (player.isShieldEnabled()) {
					collision = true;
					it.remove();
					controller.addScore(1);
				}
				else
					return true;
			}
		}
		return collision;
	}
	public void processCollision(Items items) {
		for (Enemy e : enemies)
			items.processCollision(e);
	}
	public void processCollision(Daleks controller, Piles piles) {
		// Check if any enemies hit existing piles.
		Iterator<Enemy> it = enemies.iterator();
		while (it.hasNext()) {
			Enemy e = it.next();
			if (piles.checkCollision(e)) {
				it.remove();
				controller.addScore(1);
			}
		}
		// Check if any enemies hit each other, creating a new pile.
		Object[] es = enemies.toArray();
		for (int i = 0; i < es.length; i++) {
			for (int j = i+1; j < es.length; j++) {
				Enemy esi = (Enemy) es[i];
				Enemy esj = (Enemy) es[j];
				if (esi.checkCollision(esj)) {
					enemies.remove(es[i]);
					enemies.remove(es[j]);
					if (piles.checkCollision(esi)) {
						controller.addScore(1); // We're double-counting
					}
					else
						controller.addScore(3); // 1 for each enemy and 1 for the pile
					piles.add(esi.getX(), esi.getY());
				}
			}
		}
	}
	boolean checkCollision(int x, int y) {
		for (Enemy e : enemies)
			if (e.getX() == x && e.getY() == y)
				return true;
		return false;
	}
	public boolean checkTeleportCollision(int x, int y) {
		for (Enemy e : enemies)
			if (Utility.within(e.getX(), 1, x) && Utility.within(e.getY(), 1, y))
				return true;
		return false;
	}
	public boolean near(Player p) {
		for (Enemy e : enemies) {
			if (Utility.within(e.getX(), 1, p.getX()) && Utility.within(e.getY(), 1, p.getY()))
				return true;
		}
		return false;
	}

	public void bomb(Daleks controller, Player player, Piles piles) {
		Iterator<Enemy> it = enemies.iterator();
		while (it.hasNext()) {
			Enemy e = it.next();
			if (Utility.within(e.getX(), 1, player.getX()) && Utility.within(e.getY(), 1, player.getY())) {
				piles.add(e.getX(), e.getY());
				it.remove();
				controller.addScore(1);
			}
		}
	}

	public void move(Player player) {
		for (Enemy e : enemies)
			e.move(player);
	}

	public void draw(Graphics g) {
		for (Enemy e : enemies)
			e.draw(g);
	}

}
