package pesterchum.client;

import pesterchum.client.connection.Connection;
import pesterchum.client.gui.GUI;
import pesterchum.client.resource.ResourceLoader;

public class Launch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//new GUI();
		Connection c = new Connection(new GUI());
		c.connect("localhost", 7423);
		c.login("tim", "password");
	}

}
