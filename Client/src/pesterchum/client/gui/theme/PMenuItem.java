package pesterchum.client.gui.theme;

import java.awt.Color;

import javax.swing.JMenuItem;

public class PMenuItem extends JMenuItem{
	private static final long serialVersionUID = -4750402950421416124L;
	public PMenuItem(String s){
		super(s.toUpperCase());
		this.setBackground(Color.ORANGE);
	}
}
