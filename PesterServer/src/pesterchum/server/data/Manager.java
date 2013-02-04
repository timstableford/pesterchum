package pesterchum.server.data;

import java.sql.SQLException;
import java.util.Hashtable;

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
	public void registerUser(String user, Connection conn){
		connected.put(user, conn);
	}
	public void removeUser(String user){
		if(user!=null&&connected.contains(user)){
			connected.remove(user);
		}
	}
	public Database getDatabase(){
		return database;
	}
	public void sendMessage(Message message){
		if(connected.containsKey(message.getTo())){
			Connection o = connected.get(message.getTo());
			o.sendData(Util.jsonToString(message.getJson()), true);
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
