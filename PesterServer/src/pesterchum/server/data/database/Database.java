package pesterchum.server.data.database;

import pesterchum.server.data.User;

public interface Database {
	public boolean userExists(User user);
	public boolean authenticate(User user, String password);
	public boolean newUser(User user, String password);
	public void saveUser(User user);
	
}
