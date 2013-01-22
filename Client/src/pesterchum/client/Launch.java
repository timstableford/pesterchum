package pesterchum.client;


import pesterchum.client.data.Interface;
import pesterchum.client.data.Message;
import pesterchum.client.gui.GUI;
import pesterchum.client.gui.login.Login;

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
		i.sendMessage(new Message("tim", "tom", "hello tom"));
		new Login(null,null);
	}

}
