package pesterchum.client.connection;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import argo.jdom.JsonNode;
import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonObjectNodeBuilder;
import argo.jdom.JsonRootNode;

import pesterchum.client.Util;
import pesterchum.client.data.ICData;
import pesterchum.client.data.Incoming;
import pesterchum.client.data.Message;
import pesterchum.client.gui.GUI;

public class Interface implements Incoming{
	private static final long TIMEOUT = 10000;
	private static final int VERSION = 1;
	private GUI gui;
	private Connection conn;
	private long lastPing;
	private Settings settings;
	private LinkedList<String> friends;
	public Interface(GUI gui){
		this.gui = gui;
		this.conn = new Connection();
		String s = System.getProperty("file.separator");
		try {
			this.settings = new Settings(new File(System.getProperty("user.home")+s+".pesterchum"+s+"settings.json"));
		} catch (IOException e1) {
			System.err.println("Error loading settings");
		}
		conn.registerIncoming("hello", this);
		conn.registerIncoming("message", this);
		conn.registerIncoming("admin", this);
		friends = new LinkedList<String>();
		lastPing = System.currentTimeMillis();
	}
	public Settings getSettings(){
		return settings;
	}
	public void register(String u, String p){
		//TODO
	}
	public boolean authenticated(){
		if(conn.getUsername()!=null){
			return true;
		}
		return false;
	}
	public boolean timeout(){
		if((System.currentTimeMillis()-lastPing)>TIMEOUT){
			return true;
		}
		return false;
	}
	@Override
	public void processIncoming(ICData data) {
		if(authenticated()){
			switch(data.getName()){
			case "message":
				Message m = new Message(data);
				gui.incomingMessage(m);
				break;
			case "admin":
				processAdmin(data);
				break;
			default:
				System.err.println("Unknown data from - "+data.getData());
			}
		}else{
			switch(data.getName()){
			case "hello":
				processHello(data);
				break;
			case "admin":
				processAdmin(data);
				break;
			default:
				System.err.println("Unknown data from - "+data.getData());
			}
		}
	}
	public void login(String username, String password){
		JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder()
				.withField("class", JsonNodeBuilders.aStringBuilder("admin"))
				.withField("command", JsonNodeBuilders.aStringBuilder("login"))
				.withField("username", JsonNodeBuilders.aStringBuilder(username))
				.withField("password", JsonNodeBuilders.aStringBuilder(password));
		conn.sendData(Util.jsonToString(builder.build()));
	}
	public boolean connect(String host, int port){
		conn.disconnect();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			//insomniac thread
		}
		return conn.connect(host, port);
	}
	public void sendMessage(Message message){
		conn.sendData(Util.jsonToString(message.getJson()));
	}
	public void addFriend(String username){
		JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder()
				.withField("class", JsonNodeBuilders.aStringBuilder("admin"))
				.withField("command", JsonNodeBuilders.aStringBuilder("friendrequest"))
				.withField("username", JsonNodeBuilders.aStringBuilder(Encryption.encode(username.getBytes())));
		conn.sendData(Util.jsonToString(builder.build()));
	}
	private void processFriendResponse(ICData data){
		String username = new String(Encryption.decode(data.getData().getStringValue("username")));
		boolean suc = Boolean.parseBoolean(data.getData().getStringValue("success"));
		gui.friendRequestResponse(username, suc);
	}
	private void processAdmin(ICData data){
		switch(data.getData().getStringValue("command")){
		case "disconnect":
			conn.disconnect();
			break;
		case "ping":
			JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder() 
				.withField("class", JsonNodeBuilders.aStringBuilder("admin"))
				.withField("command", JsonNodeBuilders.aStringBuilder("pong"));
			conn.sendData(Util.jsonToString(builder.build()));
			lastPing = System.currentTimeMillis();
			break;
		case "friendresponse":
			processFriendResponse(data);
			break;
		case "login":
			processLogin(data);
			break;
		default:
			System.err.println("Unknown admin command");
		}
	}
	private boolean processLogin(ICData data){
		System.out.println("Login response received");
		String un = data.getData().getStringValue("username");
		boolean suc = Boolean.parseBoolean(data.getData().getStringValue("success"));
		if(suc){
			conn.setUsername(un);
			List<JsonNode> friends = data.getData().getArrayNode("friends");
			for(JsonNode f: friends){
				this.friends.add(f.getStringValue("username"));
			}
		}
		gui.loginResponse(suc);
		return suc;
	}
	private void processHello(ICData data){
		int ver = Integer.parseInt(data.getData().getStringValue("version"));
		byte[] key = Encryption.decode(data.getData().getStringValue("key"));
		conn.getEncryption().initSymmetric(key);
		if(ver!=VERSION){
			gui.versionMismatch(VERSION, ver);
		}
		System.out.println("Received symmetric key from server");
	}
}
