package pesterchum.client.gui.main.theme;

import java.awt.Color;

import javax.swing.JPasswordField;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class PPasswordField extends JPasswordField{
	private static final long serialVersionUID = 9072624564075016973L;

	public PPasswordField(int a){
		super(a);
		Border b = new LineBorder(new Color(255,140,0), 2);
		this.setBorder(b);
	}
}
