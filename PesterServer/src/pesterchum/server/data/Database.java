package pesterchum.server.data;

import java.util.Hashtable;


public class Database {
	private Hashtable<String, Interface> interfaces;
	public Database(){
		interfaces = new Hashtable<String, Interface>();
	}
	public boolean authenticate(User user, String password){
		boolean authenticated = true;
		/*
		 * 1)Hash Password
		 * 2)Check against database
		 * 4)Set authenticated
		 * 3)Dispose of password
		 */
		user.setAuthenticated(true);
		return authenticated;
	}
	public void sendMessage(Message message){
		//TODO
		/*
		 * 1)Check if user online, if yes, send
		 * 2)If user not online, commit to database
		 */
	}
	public void registerInterface(String name, Interface inter){
		interfaces.put(name, inter);
	}
	public Interface getInterface(String name){
		return interfaces.get(name);
	}
}
