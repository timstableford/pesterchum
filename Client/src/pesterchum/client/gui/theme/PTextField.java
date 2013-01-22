package pesterchum.client.gui.theme;

import java.awt.Color;

import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class PTextField extends JTextField{
	public PTextField(int a){
		super(a);
		Border b = new LineBorder(new Color(255,140,0), 2);
		this.setBorder(b);
	}
}
