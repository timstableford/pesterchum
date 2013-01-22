package pesterchum.client.gui.theme;

import java.awt.Color;

import javax.swing.JPasswordField;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class PPasswordField extends JPasswordField{
	public PPasswordField(int a){
		super(a);
		Border b = new LineBorder(new Color(255,140,0), 2);
		this.setBorder(b);
	}
}
