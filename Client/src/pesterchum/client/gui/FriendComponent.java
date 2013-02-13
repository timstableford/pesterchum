package pesterchum.client.gui;

import java.awt.Color;

import javax.swing.JLabel;

public class FriendComponent extends JLabel{
	private static final long serialVersionUID = 1L;
	private String username;
	public FriendComponent(String username){
		super(username);
		this.setBackground(Color.black);
		this.username = username;
	}
	public String getUsername(){
		return username;
	}
}
