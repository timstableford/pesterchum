package pesterchum.server.data;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;


import pesterchum.server.Connection;
import pesterchum.server.Util;
import pesterchum.server.data.database.Database;


public class Manager {
	private Hashtable<String, Interface> interfaces;
	private Hashtable<String, Connection> connected;
	private Database database;
	public Manager(Database database){
		interfaces = new Hashtable<String, Interface>();
		connected = new Hashtable<String, Connection>();
		this.database = database;
	}
	public static Packet parsePacket(ICData packet) throws IOException{
		switch(packet.getData().getStringValue("type")){
		case "message":
			return new Message(packet);
		default:
			return null;
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
		List<Packet> packets = database.getPackets(user);
		while(packets.size()>0){
			Packet m = packets.remove(0);
			sendPacket(m);
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
	public void sendPacket(Packet packet){
		if(connected.containsKey(packet.getTo())){
			Connection o = connected.get(packet.getTo());
			o.getConn().write(Util.jsonToString(packet.getJson().build()));
		}else{
			if(packet.allowsOffline()){
				database.storePacket(packet);
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
