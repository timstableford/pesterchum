package pesterchum.client.gui.theme;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class PLabelButton extends PLabel implements MouseListener{
	private ActionListener l;
	private static final long serialVersionUID = -7283571591197653501L;
	public PLabelButton(){
		super();
		l = null;
	}
	public PLabelButton(String text){
		super(text);
		l = null;
	}
	public void addActionListener(ActionListener l){
		this.l = l;
	}
	@Override
	public void mouseClicked(MouseEvent e) {}
	@Override
	public void mousePressed(MouseEvent e) {
		if(l!=null){
			l.actionPerformed(new ActionEvent(this, 0, this.getText()));
		}
	}
	@Override
	public void mouseReleased(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
}
