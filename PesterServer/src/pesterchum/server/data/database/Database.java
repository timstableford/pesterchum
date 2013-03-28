package pesterchum.server.data.database;

import java.util.List;

import pesterchum.server.data.Packet;
import pesterchum.server.data.User;

public interface Database {
	public boolean userExists(String username);
	public boolean authenticate(User user, String password);
	public boolean newUser(User user, String password);
	public void storePacket(Packet m);
	public List<Packet> getPackets(String username);
	public void saveUser(User user);
}
