package pesterchum.client.gui.theme;

import java.awt.Color;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class PButton extends JButton{
	private static final long serialVersionUID = 4446906053611098414L;
	public PButton(String string) {
		//TODO whent he button is clicked stop it from being blue
		super(string.toUpperCase());
		Border b = new LineBorder(new Color(255,140,0), 2);
		this.setBackground(Color.YELLOW);
		this.setBorder(b);
	}
	public void addActionListener(ActionListener l){
		super.addActionListener(l);
	}

}
