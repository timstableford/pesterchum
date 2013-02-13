package pesterchum.client.gui;

import java.awt.Component;

import pesterchum.client.connection.Interface;
import pesterchum.client.data.Message;
import pesterchum.client.gui.theme.PFrame;
//TODO alex make this do shiny things, k?
public class Messaging extends PFrame{
	private static final long serialVersionUID = 1L;
	private String user;
	public Messaging(Interface ifa, String user, Component parent){
		this.setLocationRelativeTo(parent);
		this.setSize(400,300);
		this.setTitle(user);
		this.user = user;
		this.setVisible(true);
	}
	public String getUser(){
		return user;
	}
	public void incoming(Message message){
		//TODO link this to non-existent gui
	}
}
