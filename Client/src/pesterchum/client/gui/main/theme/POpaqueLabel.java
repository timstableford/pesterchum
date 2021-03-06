package pesterchum.client.gui.main.theme;

import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class POpaqueLabel extends JLabel{
	private static final long serialVersionUID = 1L;
	public POpaqueLabel(String text, int alignment){
		super(text, alignment);
		this.setOpaque(true);
		this.setBackground(Color.ORANGE);
		Font f = new Font("Aerial", Font.BOLD, 12);
		this.setHorizontalAlignment(JLabel.LEFT);
		this.setForeground(Color.BLACK);
		this.setFont(f);
		this.setText(text.toUpperCase());
	}
	public POpaqueLabel(){
		super();
		this.setOpaque(true);
		this.setBackground(Color.ORANGE);
		Font f = new Font("Aerial", Font.BOLD, 12);
		this.setHorizontalAlignment(JLabel.LEFT);
		this.setForeground(Color.BLACK);
		this.setFont(f);
	}
	public POpaqueLabel(String text){
		this();
		this.setText(text.toUpperCase());
	}
	public POpaqueLabel(String text, ImageIcon icon){
		this();
		this.setIcon(icon);
		this.setText(text);
	}
}
