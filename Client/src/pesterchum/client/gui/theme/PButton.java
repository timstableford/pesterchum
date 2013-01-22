package pesterchum.client.gui.theme;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class PButton extends JButton implements MouseListener{
	private static final long serialVersionUID = 4446906053611098414L;
	private Color NORMAL = Color.yellow;
	private Color CLICKED = Color.orange;
	private ActionListener l;
	public PButton(String string){
		super(string.toUpperCase());
		l = null;
		Border b = new LineBorder(new Color(255,140,0), 2);
		this.setBackground(NORMAL);
		this.setBorder(b);
		this.setContentAreaFilled(false);
        this.setOpaque(true);
        this.addMouseListener(this);
        this.setFocusable(false);
	}
	public void unclickedColor(Color c){
		NORMAL = c;
	}
	public void clickedColor(Color c){
		CLICKED = c;
	}
	@Override
	public void addActionListener(ActionListener l){
		this.actionListener = l;
	}
	@Override
	public void mousePressed(MouseEvent e) {
		this.setBackground(CLICKED);
		if(l!=null){
			l.actionPerformed(new ActionEvent(this, 0, this.getText()));
		}
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		this.setBackground(NORMAL);
	}
	@Override
	public void mouseClicked(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}

}
