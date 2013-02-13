package pesterchum.client.gui;

import javax.swing.Box;
import javax.swing.JScrollPane;

import pesterchum.client.connection.Interface;

public class FriendPane extends JScrollPane{
	private static final long serialVersionUID = 1L;
	private Interface ifa;
	private Box box;
	public FriendPane(Interface ifa){
		super();
		box = Box.createVerticalBox();
		super.setViewportView(box);
		this.ifa = ifa;
		this.redraw();
	}
	public void redraw(){
		box.removeAll();
		for(String f: ifa.getFriends()){
			box.add(new FriendComponent(f));
		}
		this.validate();
		this.repaint();
	}
}
