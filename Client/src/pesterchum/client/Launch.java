package pesterchum.client;

import pesterchum.client.data.Interface;
import pesterchum.client.gui.GUI;

public class Launch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//new GUI();
		Interface i = new Interface(new GUI());
		i.connect("localhost", 7423);
		i.login("tim", "password");
	}

}
