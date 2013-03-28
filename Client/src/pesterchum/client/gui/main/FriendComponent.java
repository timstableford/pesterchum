package pesterchum.client.gui.main;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;

public class FriendComponent extends JLabel implements MouseListener{
	private ActionListener l;
	private boolean entered = false;
	private static final long serialVersionUID = 1L;
	private String username;
	private boolean selected;
	public FriendComponent(String username){
		super(username);
		this.setOpaque(true);
		this.setBackground(Color.black);
		this.setForeground(Color.white);
		this.username = username;
		this.addMouseListener(this);
	}
	public String getUsername(){
		return username;
	}
	public void addActionListener(ActionListener l){
		this.l = l;
	}
	public boolean isSelected(){
		return this.selected;
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		if(l!=null){
			if(selected){
				l.actionPerformed(new ActionEvent(this, 0, "clicked"));
			}else{
				l.actionPerformed(new ActionEvent(this, 0, "selected"));
			}
		}	
	}
	public void setSelected(boolean selected){
		this.selected = selected;
		if(selected){
			this.setBackground(new Color(146, 165, 189));
		}else{
			if(entered){
				this.setBackground(Color.gray);
			}else{
				this.setBackground(Color.black);
			}
		}
	}
	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {
		if(!selected){
			this.setBackground(Color.gray);
		}
		entered = true;
	}
	@Override
	public void mouseExited(MouseEvent e) {
		if(!selected){
			this.setBackground(Color.BLACK);
		}
		entered = false;
	}
}
