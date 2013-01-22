package pesterchum.client;


import pesterchum.client.connection.Interface;
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
		//i.sendMessage(new Message("tim", "tom", "hello tom"));
		new Login(i);
	}

}
