package pesterchum.server.data;

import java.util.Hashtable;

import pesterchum.server.Connection;


public class Database {
	private Hashtable<String, Interface> interfaces;
	private Hashtable<String, Connection> connected;
	public Database(){
		interfaces = new Hashtable<String, Interface>();
		connected = new Hashtable<String, Connection>();
	}
	public boolean authenticate(User user, String password){
		boolean authenticated = true;
		//TODO implement authentication
		/*
		 * 1)Hash Password
		 * 2)Check against database
		 * 4)Set authenticated
		 * 3)Dispose of password
		 */
		user.setAuthenticated(true);
		return authenticated;
	}
	public void registerUser(String user, Connection conn){
		connected.put(user, conn);
	}
	public void removeUser(String user){
		if(user!=null&&connected.contains(user)){
			connected.remove(user);
		}
	}
	public void sendMessage(Message message){
		//TODO
		/*
		 * 1)Check if user online, if yes, send
		 * 2)If user not online, commit to database
		 */
		if(connected.containsKey(message.getTo())){
			Connection o = connected.get(message.getTo());
			o.sendData(message.getXML());
		}else{
			//TODO Add to database
			System.out.println("Could not send message user offline - "+message.getTo());
		}
	}
	public void registerInterface(String name, Interface inter){
		interfaces.put(name, inter);
	}
	public Interface getInterface(String name){
		return interfaces.get(name);
	}
}
