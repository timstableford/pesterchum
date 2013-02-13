package pesterchum.client.gui;

import pesterchum.client.connection.Interface;
import pesterchum.client.gui.theme.PFrame;
//TODO alex make this do shiny things, k?
public class Messaging extends PFrame{
	private static final long serialVersionUID = 1L;
	public Messaging(Interface ifa, String user){
		this.setVisible(true);
		this.setSize(400,300);
		this.setTitle(user);
	}
}
