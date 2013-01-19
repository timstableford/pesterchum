package pesterchum.client.gui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.Border;

public class PLabel extends JLabel{
	private static final long serialVersionUID = 1L;
	public PLabel(){
		super();
		Border b = BorderFactory.createLineBorder(Color.YELLOW, 2);
		this.setBorder(b);
		this.setOpaque(true);
		this.setBackground(Color.BLACK);
		Font f = new Font("Serif", Font.BOLD, 16);
		this.setHorizontalAlignment(JLabel.CENTER);
		this.setForeground(Color.WHITE);
		this.setFont(f);
	}
	public PLabel(String text){
		this();
		this.setText(text.toUpperCase());
	}
	public PLabel(String text, ImageIcon icon){
		this();
		this.setIcon(icon);
		this.setText(text);
	}
}
