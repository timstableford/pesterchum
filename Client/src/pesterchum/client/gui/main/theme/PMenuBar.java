package pesterchum.client.gui.main.theme;

import java.awt.Color;

import javax.swing.JMenuBar;
import javax.swing.border.LineBorder;;

public class PMenuBar extends JMenuBar{
	private static final long serialVersionUID = 1413101018314332190L;
	public PMenuBar(){
		super();
		this.setBackground(Color.ORANGE);
		this.setBorder(new LineBorder(Color.YELLOW,0));
	}
}
