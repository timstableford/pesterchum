package pesterchum.client.gui.theme;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class PButton extends JButton{

	public PButton(String string) {
		super(string);
		Border b = new LineBorder(new Color(255,140,0), 2);
		this.setBackground(Color.YELLOW);
		this.setBorder(b);
	}

}
