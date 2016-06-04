package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;

public class TaskCheckBox extends JCheckBox {
	
	public TaskCheckBox(String text) {
		super();
		this.setText(text);
		this.setPreferredSize(new Dimension(100, 20));
		this.setBackground(Color.YELLOW);
	}
	
}
