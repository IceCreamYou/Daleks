import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;


public class GamePanel extends JPanel {

	//=======================================================INSTANCE VARIABLES

	public static final int EXPAND_LEVEL = 20;

	private static final int REFRESH_INTERVAL = 1000;

	private static int gridWidth = 20, gridHeight = 20, actorWidth = 30, actorHeight = 30;

	private int level = 1;

	private boolean gameOver = false;

	private Player player;
	private Enemies enemies;
	private Items items;
	private Piles piles;

	private Daleks controller;

	/**
	 * Required for subclasses of Swing components.
	 */
	private static final long serialVersionUID = 1552746400473185110L;

	//==============================================================CONSTRUCTOR

	public GamePanel(Daleks controller) {
		this.controller = controller;

		// Determine the screen size and set the window size appropriately.
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int size = (int) (Math.min(screen.getWidth(), screen.getHeight()) * 0.8);

		// Set properties.
		setPreferredSize(new Dimension(size, size));
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		setFocusable(true);
		setBackground(Color.WHITE);
		grabFocus();

		// Set up everything that exists "in" the game.
		player = new Player(gridWidth / 2, gridHeight / 2);
		enemies = new Enemies(level, player);
		items = new Items(level, player, enemies);
		piles = new Piles();

		// React when the users provides input via the mouse or keyboard.
		addInputListeners();

		// Refresh every so often, just in case.
		Timer timer = new Timer(REFRESH_INTERVAL, new ActionListener() {
			public void actionPerformed(ActionEvent e) { repaint(); }
		});
		timer.start();
	}

	//===============================================================PROPERTIES

	public boolean isGameOver() {
		return gameOver;
	}

	public int getLevel() {
		return level;
	}

	public Player getPlayer() {
		return player;
	}

	public Enemies getEnemies() {
		return enemies;
	}

	public Items getItems() {
		return items;
	}

	public Piles getPiles() {
		return piles;
	}

	public static int getGridWidth() {
		return gridWidth;
	}

	public static int getGridHeight() {
		return gridHeight;
	}

	public static int getActorWidth() {
		return actorWidth;
	}

	public static int getActorHeight() {
		return actorHeight;
	}

	public static void setGridDimensions(int w, int h) {
		gridWidth = w;
		gridHeight = h;
	}

	public static void computeActorDimensions(Graphics g) {
		Rectangle b = g.getClipBounds();
		double w = b.getWidth(), h = b.getHeight();
		actorWidth = (int) (w / gridWidth);
		actorHeight = (int) (h / gridHeight);
	}

	//===================================================================UPDATE

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		computeActorDimensions(g);

		drawHints(g);
		drawGrid(g);
		items.draw(g);
		piles.draw(g);
		enemies.draw(g);
		player.draw(g);
	}

	private void drawGrid(Graphics g) {
		g.setColor(Color.BLACK);
		for (int i = 0; i <= gridWidth; i++) {
			g.drawLine(0, i * actorHeight, gridWidth * actorWidth, i * actorHeight);
		}
		for (int i = 0; i <= gridHeight; i++) {
			g.drawLine(i * actorWidth, 0, i * actorWidth, gridHeight * actorHeight);
		}
	}

	private void drawHints(Graphics g) {
		g.setColor(new Color(220, 230, 250));
		for (int i = player.getX() - 1; i <= player.getX() + 1; i++) {
			for (int j = player.getY() - 1; j <= player.getY() + 1; j++) {
				if (i >= 0 && i < gridWidth && j >= 0 && j < gridHeight) { // inside the grid
					if (!enemies.checkCollision(i, j) && !piles.checkCollision(i, j)) // not under enemies or piles
						g.fillRect(i * actorWidth, j * actorHeight, actorWidth, actorHeight);
				}
			}
		}
	}

	// The return value means "the level was not completed."
	boolean tick() {
		boolean lose = false;
		items.processCollision(player, this);					// Check if the player picked up items.
		if (piles.checkCollision(player))						// Check if the player ran into piles.
			lose = true;
		enemies.move(player);									// Move the enemies.
		if (enemies.processCollision(controller, player)) {		// Check if the player ran into enemies.
			if (player.isShieldEnabled())
				player.toggleShield();
			else
				lose = true;
		}
		enemies.processCollision(items);						// Check if the enemies trampled items.
		enemies.processCollision(controller, piles);			// Check if the enemies ran into piles or other enemies.

		repaint();
		if (lose)
			lose();
		else if (enemies.getSize() == 0) {						// If there are no enemies left, the level ends.
			win();
			return false;
		}
		else
			controller.updateLabels();

		return true;
	}

	//==================================================================ACTIONS

	public void lose() {
		// If the player has lives, restart the level.
		if (player.getLives() > 0) {
			player.resetPosition();
			player.resetItems(level);
			player.useItem(this, Item.ItemType.LIFE);
			enemies = new Enemies(level, player);
			items = new Items(level, player, enemies);
			piles = new Piles();
			repaint();
			controller.updateLabels();
			return;
		}

		// If we have a new high score, save it and alert the player.
		controller.highScore();

		// The game is over; ask the player what to do.
		player.useItem(this, Item.ItemType.LIFE);
		Object[] options = {"Play again", "Retry level", "Close game"};
		int n = JOptionPane.showOptionDialog(
				getParent(),
				"You died! What would you like to do?",
				"Game Over",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				options,
				options[0]
		);

		// Respond to the player's choice.
		gameOver = true;
		if (n == JOptionPane.YES_OPTION) {						// play again
			resetLevel();
			player.resetPosition();
			player.resetItems(level);
			enemies = new Enemies(level, player);
			items = new Items(level, player, enemies);
			piles = new Piles();
			gameOver = false;
			repaint();
			controller.resetToolbarButtons();
			controller.updateLabels();
		}
		else if (n == JOptionPane.NO_OPTION) {					// retry level
			player.clearItems();
			player.resetPosition();
			player.resetItems(level);
			enemies = new Enemies(level, player);
			items = new Items(level, player, enemies);
			piles = new Piles();
			gameOver = false;
			repaint();
			controller.resetScore();
			controller.resetToolbarButtons();
			controller.updateLabels();
		}
		else if (n == JOptionPane.CANCEL_OPTION) {				// close the game
			System.exit(0);
		}
		else if (n == JOptionPane.CLOSED_OPTION) {				// close the dialog box
			// do nothing
			repaint();
			controller.updateLabels();
		}
	}

	/** Go to the next level. */
	public void win() {
		level++;
		controller.addScore(5 + 5 * level);
		if (level % EXPAND_LEVEL == 0)
			expand();
		resetBoard();
	}

	public void resetBoard() {
		player.resetPosition();
		player.resetItems(level);
		enemies = new Enemies(level, player);
		items = new Items(level, player, enemies);
		piles = new Piles();
		repaint();
		controller.updateLabels();
	}

	/** Reset to the first level. */
	void resetLevel() {
		level = 1;
		gridWidth = 20;
		gridHeight = 20;
		player.clearItems();
		controller.resetScore();
		gameOver = false;
		controller.resetToolbarButtons();
		controller.updateLabels();
	}

	public void useItem(Item.ItemType type) {
		switch(type) {
		case LIFE: break; // Do nothing. We already took away a life and reset.
		case BOMB:
			enemies.bomb(controller, player, piles); // destroy enemies
			items.bomb(player); // destroy items
			break;
		case PILE:
			piles.add(player.getX(), player.getY()); // drop a pile
			repaint(); // add a visual indication that the pile was dropped
			break;
		case SHIELD:
			if (!player.isShieldEnabled()) // enable shield
				player.toggleShield();
			break;
		case SAFE_TELEPORT:
			player.teleport(enemies, items, piles, true); // teleport player
			break;
		}
	}

	public void sit() {
		while (!enemies.near(player) && tick()) ;
	}

	public String getScore(Item.ItemType type, Player p, Enemies e, int level) {
		if (type != null && (p == null || e == null))
			throw new RuntimeException("Cannot get score string for "+ type);
		else if (type == null) { // This is a sloppy hack.
			if (level < 0)
				return e.getSize() +" enemies left";
			return "Level "+ level;
		}
		switch (type) {
		case BOMB: return "Bombs: "+ p.getItemCount(Item.ItemType.BOMB); 
		case LIFE: return "Lives: "+ p.getLives(); 
		case SAFE_TELEPORT: return "Safe teleports: "+ p.getItemCount(Item.ItemType.SAFE_TELEPORT); 
		case SHIELD: return p.isShieldEnabled() ? "Shield enabled" : "Shield disabled";
		case PILE: return "Porta-piles: "+ p.getItemCount(Item.ItemType.PILE);
		default: return null;
		}
	}

	public void expand() {
		controller.addScore(250 * (level / EXPAND_LEVEL));
		gridWidth *= Math.sqrt(2); // Double the number of squares.
		gridHeight *= Math.sqrt(2);
		repaint();
	}

	//====================================================================INPUT

	private void addInputListeners() {
		final GamePanel g = this;
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (!gameOver) {
					if ("123456789".indexOf(e.getKeyChar()) >= 0) {
						player.move(e);
						tick();
					}
					else if ("-sp".indexOf(e.getKeyChar()) >= 0)
						player.useItem(g, Item.ItemType.PILE);
					else if (".bd".indexOf(e.getKeyChar()) >= 0 && player.getItemCount(Item.ItemType.BOMB) > 0) {
						player.useItem(g, Item.ItemType.BOMB);
						tick();
					}
					else if ("0ft ".indexOf(e.getKeyChar()) >= 0) {
						if (player.getItemCount(Item.ItemType.SAFE_TELEPORT) > 0)
							player.useItem(g, Item.ItemType.SAFE_TELEPORT);
						else
							player.teleport(enemies, items, piles, false);
						tick();
					}
					else if ("w+".indexOf(e.getKeyChar()) >= 0)
						sit();
				}
				else if (e.getKeyChar() == 'r') {
					controller.resetHighScore();
				}
			}
		});
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (!gameOver) {
					player.move(e);
					tick();
				}
			}
		});
	}

}
