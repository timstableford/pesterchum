package pesterchum.client;

import pesterchum.client.connection.Interface;
import pesterchum.client.data.Message;

public interface PesterchumGUI {
	public void init(Interface ifa);
	public void incomingMessage(Message message);
	public void loginResponse(boolean success);
	public void friendRequestResponse(String username, boolean found);
	public void timeout();
}
