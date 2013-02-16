package pesterchum.server.data.database;

import java.util.List;

import pesterchum.server.data.Message;
import pesterchum.server.data.User;

public interface Database {
	public boolean userExists(String username);
	public boolean authenticate(User user, String password);
	public boolean newUser(User user, String password);
	public void storeMessage(Message m);
	public List<Message> getMessages(String username);
	public void saveUser(User user);
}
