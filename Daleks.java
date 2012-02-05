import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * Daleks!
 *
 * TODO
 * - Create a "magnet" powerup that draws other powerups closer to you on each turn.
 */
public class Daleks {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Daleks();
			}
		});
	}


	private static Preferences prefs;

	private static int score = 0;
	private static int highScore = 0;
	private static int origHighScore = 0;
	private static final String HIGH_SCORE_KEY = "daleksHighScore";

	private static final int TOOLBAR_IMAGE_SIZE = 20;

	private final JFrame frame;
	private final GamePanel gamePanel;
	private final JPanel scorePanel;
	private final JPanel infoPanel;
	private final JLabel bombLabel = new JLabel();
	private final JLabel enemiesLabel = new JLabel();
	private final JLabel highScoreLabel = new JLabel();
	private final JLabel levelLabel = new JLabel();
	private final JLabel lifeLabel = new JLabel();
	private final JLabel pileLabel = new JLabel();
	private final JLabel scoreLabel = new JLabel();
	private final JLabel teleportLabel = new JLabel();
	private final JButton bombButton;
	private final JButton teleportButton;
	private final JButton pileButton;
	private final JButton waitButton;

	public Daleks() {
		// Get the user preferences where we store the high score.
		prefs = Preferences.userRoot().node(getClass().getName());
		origHighScore = prefs.getInt(HIGH_SCORE_KEY, 0);
		highScore = origHighScore;

    	// Set up the window.
    	frame = new JFrame("Daleks");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(new ImageIcon(Daleks.class.getResource("images/enemy.png")).getImage());

		// Set up the panel in which the game is actually played.
		gamePanel = new GamePanel(this);
		frame.add(gamePanel, BorderLayout.CENTER);

		JPanel north = new JPanel();
		north.setLayout(new BorderLayout());
		// Set up the panel in which game information is displayed.
		scorePanel = new JPanel();
		infoPanel = new JPanel();
		north.add(scorePanel, BorderLayout.NORTH);
		north.add(infoPanel, BorderLayout.CENTER);
		frame.add(north, BorderLayout.NORTH);
		Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
		levelLabel.setFont(labelFont);
		scorePanel.add(levelLabel);
		enemiesLabel.setFont(labelFont);
		scorePanel.add(enemiesLabel);
		scoreLabel.setFont(labelFont);
		scorePanel.add(scoreLabel);
		highScoreLabel.setFont(labelFont);
		scorePanel.add(highScoreLabel);
		lifeLabel.setFont(labelFont);
		infoPanel.add(lifeLabel);
		bombLabel.setFont(labelFont);
		infoPanel.add(bombLabel);
		teleportLabel.setFont(labelFont);
		infoPanel.add(teleportLabel);
		pileLabel.setFont(labelFont);
		infoPanel.add(pileLabel);

		// Set up the toolbar.
		final JToolBar toolbar = new JToolBar("Actions");
		north.add(toolbar, BorderLayout.SOUTH);
		toolbar.setBorder(new EmptyBorder(0, 207, 0, 0)); // (600 - (6 * 30 + 5)) / 2
		bombButton = makeToolbarButton("bomb.png", "Explode bomb", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (gamePanel.isGameOver())
					return;
				Player player = gamePanel.getPlayer();
				if (player.getItemCount(Item.ItemType.BOMB) > 0) {
					player.useItem(gamePanel, Item.ItemType.BOMB);
					gamePanel.tick();
				}
		        gamePanel.requestFocusInWindow();
			}
		});
		toolbar.add(bombButton);
		teleportButton = makeToolbarButton("teleporter-safe.png", "Safe teleport", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (gamePanel.isGameOver())
					return;
				Player player = gamePanel.getPlayer();
				if (player.getItemCount(Item.ItemType.SAFE_TELEPORT) > 0)
					player.useItem(gamePanel, Item.ItemType.SAFE_TELEPORT);
				else
					player.teleport(gamePanel.getEnemies(), gamePanel.getItems(), gamePanel.getPiles(), false);
				gamePanel.tick();
		        gamePanel.requestFocusInWindow();
			}
		});
		toolbar.add(teleportButton);
		pileButton = makeToolbarButton("pile-item.png", "Drop pile", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (gamePanel.isGameOver())
					return;
				Player player = gamePanel.getPlayer();
				int count = player.getItemCount(Item.ItemType.PILE);
				if (count > 0)
					player.useItem(gamePanel, Item.ItemType.PILE);
				if (count == 1)
					pileButton.setEnabled(false);
		        gamePanel.requestFocusInWindow();
			}
		});
		toolbar.add(pileButton);
		waitButton = makeToolbarButton("wait.png", "Stay here as long as possible without dying", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (gamePanel.isGameOver())
					return;
				gamePanel.sit();
		        gamePanel.requestFocusInWindow();
			}
		});
		toolbar.add(waitButton);
		toolbar.add(makeToolbarButton("restart.png", "Play a new game", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				highScore();
				gamePanel.resetLevel();
				gamePanel.resetBoard();
		        gamePanel.requestFocusInWindow();
			}
		}));
		toolbar.add(makeToolbarButton("instructions.png", "Instructions", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(frame, getInstructions());
		        gamePanel.requestFocusInWindow();
		        gamePanel.repaint();
			}
		}));

		// When the window is resized, move the toolbar to the center.
		frame.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				int toolbarCount = toolbar.getComponentCount();
				// sum(button widths) + gap between buttons
				int toolbarSize = toolbarCount * toolbar.getComponent(0).getWidth() + toolbarCount - 1;
				int offset = (int) ((frame.getSize().getWidth() - toolbarSize) / 2);
				toolbar.setBorder(new EmptyBorder(0, offset, 0, 0));
			}
		});

		// Put the frame on the screen
		frame.pack();
		frame.setLocationRelativeTo(null);
		updateLabels();
        frame.setVisible(true);
        gamePanel.requestFocusInWindow();
	}

	void updateLabels() {
		Player p = gamePanel.getPlayer();
		Enemies e = gamePanel.getEnemies();
		int level = gamePanel.getLevel();

		// Update the informational labels.
		bombLabel.setText(" "+ gamePanel.getScore(Item.ItemType.BOMB, p, e, level) +" ");
		enemiesLabel.setText(" "+ gamePanel.getScore(null, p, e, -1) +" ");
		highScoreLabel.setText(" High score: "+ highScore +" ");
		levelLabel.setText(" "+ gamePanel.getScore(null, p, e, level) +" ");
		lifeLabel.setText(" "+ gamePanel.getScore(Item.ItemType.LIFE, p, e, level) +" ");
		pileLabel.setText(" "+ gamePanel.getScore(Item.ItemType.PILE, p, e, level) +" ");
		scoreLabel.setText(" Score: "+ score +" ");
		teleportLabel.setText(" "+ gamePanel.getScore(Item.ItemType.SAFE_TELEPORT, p, e, level) +" ");
		scorePanel.repaint();
		infoPanel.repaint();

		// Disable buttons when you can't use them.
		if (p.getItemCount(Item.ItemType.BOMB) == 0)
			bombButton.setEnabled(false);
		else
			bombButton.setEnabled(true);
		if (p.getItemCount(Item.ItemType.SAFE_TELEPORT) == 0) {
			teleportButton.setIcon(getImageIcon("teleporter.png", "Teleport"));
			teleportButton.setToolTipText("Teleport");
		}
		else {
			teleportButton.setIcon(getImageIcon("teleporter-safe.png", "Safe teleport"));
			teleportButton.setToolTipText("Safe teleport");
		}
		if (p.getItemCount(Item.ItemType.PILE) == 0)
			pileButton.setEnabled(false);
		else
			pileButton.setEnabled(true);

		// Disable buttons when the game is over.
		if (gamePanel.isGameOver()) {
			bombButton.setEnabled(false);
			teleportButton.setEnabled(false);
			pileButton.setEnabled(false);
			waitButton.setEnabled(false);
		}
	}

	private JButton makeToolbarButton(String imagePath, String toolTip, ActionListener actionListener) {
		JButton button = new JButton();
		button.addActionListener(actionListener);
		button.setIcon(getImageIcon(imagePath, toolTip));
		button.setToolTipText(toolTip);
		return button;
	}

	private static ImageIcon getImageIcon(String imagePath, String altText) {
		ImageIcon icon;
		try {
			icon = new ImageIcon(
					ImageIO.read(
							Daleks.class.getResource("images/"+ imagePath)
					).getScaledInstance(TOOLBAR_IMAGE_SIZE, TOOLBAR_IMAGE_SIZE, Image.SCALE_SMOOTH)
			, altText);
		}
		catch (IOException e) {
			icon = new ImageIcon(Daleks.class.getResource("images/"+ imagePath), altText);
		}
		return icon;
	}

	private String getInstructions() {
		return
		"GOAL:\n"+
		"Move around or use a powerup and the Daleks will move closer to you.\n"+
		"Your goal is to destroy the Daleks by making them run into each other.\n"+
		"Daleks that collide form a pile. Daleks that run into piles die.\n"+
		"If you occupy the same area as a Dalek or a pile, you will die.\n"+
		"However, you can use (and pick up) powerups to help you.\n\n"+

		"CONTROLS:\n"+
		"Click or use the numpad to move.\n"+
		"Take other actions using the buttons at the top or one of these keys:\n"+
		" - Explode a bomb: b d .\n"+
		" - Drop a porta-pile: p s -\n"+
		" - Teleport: t f 0 <space>\n"+
		" - Wait: w\n\n"+

		"POWERUPS:\n"+
		"Bombs destroy all Daleks within one square of you and turn them into piles.\n"+
		"Dropping a porta-pile creates a new pile beneath you. Place it strategically!\n"+
		"You can drop a porta-pile and move to another location in one turn.\n"+
		"Teleports move you to a different place on the screen.\n"+
		"You have a limited number of safe teleports.\n"+
		"If you teleport without safe teleports, you could land in a deadly place!\n"+
		"Shields activate automatically. On the next turn in which a Dalek touches you,\n"+
		"you will be immune and the Dalek(s) will die without creating a pile.\n"+
		"Lives allow you to restart the current level if you die.\n"+
		"Waiting makes you stay in the same place as long as possible without dying.\n"+
		"Daleks eat powerups; they will destroy them if they enter the same space.\n\n"+

		"SCORE:\n"+
		" - 1 point for every Dalek that runs into a pile or shielded player\n"+
		" - 1 point for every Dalek that is bombed\n"+
		" - 3 points for two Daleks colliding and creating a pile\n"+
		" - 5 points for three Daleks colliding and creating a pile";
	}

	public void addScore(int amount) {
		score += amount;
		if (score > highScore)
			highScore = score;
		updateLabels();
	}

	public void resetScore() {
		score = 0;
		updateLabels();
	}

	void highScore() {
		if (score > origHighScore) {
			prefs.putInt(HIGH_SCORE_KEY, score);
			origHighScore = score;
			highScore = score;
			JOptionPane.showMessageDialog(
					frame,
					"You got a new high score!",
					"High score",
					JOptionPane.PLAIN_MESSAGE);
		}
	}

	void resetHighScore() {
		if (origHighScore > 0) {
			int result = JOptionPane.showConfirmDialog(
					frame,
					"Are you sure you want to reset the high score?",
					"Reset high score",
					JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				prefs.putInt(HIGH_SCORE_KEY, 0);
				origHighScore = 0;
				highScore = 0;
				updateLabels();
			}
			gamePanel.repaint();
		}
		gamePanel.requestFocusInWindow();
	}

	void resetToolbarButtons() {
		bombButton.setEnabled(true);
		teleportButton.setEnabled(true);
		pileButton.setEnabled(true);
		waitButton.setEnabled(true);
	}

}
