package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JProgressBar;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicProgressBarUI;

public class CustomBar extends JProgressBar {
	
	public CustomBar(Color color, String text) {
		super(SwingUtilities.HORIZONTAL);
		this.setForeground(color);
		this.setOpaque(false);
		this.setString(text);
		this.setStringPainted(true);
		this.setUI(new BasicProgressBarUI() {
		      protected Color getSelectionBackground() { return Color.black; }
		      protected Color getSelectionForeground() { return Color.black; }
		});
	}
	
}
