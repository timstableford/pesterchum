package pesterchum.client.gui.main.theme;

import java.awt.Color;

import javax.swing.JMenu;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class PMenu extends JMenu{
	private static final long serialVersionUID = -4517571892723241952L;
	public PMenu(String s){
		super(s.toUpperCase());
		Border b = new LineBorder(Color.YELLOW, 2);
		this.setBackground(Color.ORANGE);
		this.getPopupMenu().setBorder(b);
	}
}
