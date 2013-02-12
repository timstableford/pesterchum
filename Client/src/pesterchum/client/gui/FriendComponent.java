package pesterchum.client.gui;

import javax.swing.JLabel;

import pesterchum.client.gui.theme.PButton;
import pesterchum.client.gui.theme.PLabel;

public class FriendComponent extends PButton{
	private static final long serialVersionUID = 1L;
	private String username;
	public FriendComponent(String username){
		super(username);
		this.username = username;
	}
	public String getUsername(){
		return username;
	}
}
