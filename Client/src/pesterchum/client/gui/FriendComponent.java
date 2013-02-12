package pesterchum.client.gui;

import pesterchum.client.gui.theme.PButton;

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
