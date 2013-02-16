package pesterchum.server.data;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

import pesterchum.server.Connection;
import pesterchum.server.Util;
import pesterchum.server.data.database.Database;
import pesterchum.server.data.database.SQLiteDatabase;


public class Manager {
	private Hashtable<String, Interface> interfaces;
	private Hashtable<String, Connection> connected;
	private Database database;
	public Manager(){
		interfaces = new Hashtable<String, Interface>();
		connected = new Hashtable<String, Connection>();
		try {
			database = new SQLiteDatabase("database.db");
		} catch (ClassNotFoundException | SQLException e) {
			System.err.println("Could not open database");
			e.printStackTrace();
		}
	}
	public boolean authenticate(User user, String password){
		return database.authenticate(user, password);
	}
	public boolean register(User user, String password){
		return database.newUser(user, password);
	}
	public void registerUser(String user, Connection conn){
		connected.put(user, conn);
		//load offline messages from db
		List<Message> messages = database.getMessages(user);
		while(messages.size()>0){
			Message m = messages.remove(0);
			sendMessage(m);
		}
	}
	public void removeUser(String user){
		if(user!=null&&connected.containsKey(user)){
			connected.remove(user);
		}
	}
	public Database getDatabase(){
		return database;
	}
	public void sendMessage(Message message){
		if(connected.containsKey(message.getTo())){
			Connection o = connected.get(message.getTo());
			o.getConn().write(Util.jsonToString(message.getJson()));
		}else{
			if(message.allowsOffline()){
				database.storeMessage(message);
			}
		}
	}
	public void registerInterface(String name, Interface inter){
		interfaces.put(name, inter);
	}
	public Interface getInterface(String name){
		return interfaces.get(name);
	}
}
