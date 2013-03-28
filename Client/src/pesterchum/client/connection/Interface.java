package pesterchum.client.connection;

import java.io.File;
import java.io.IOException;

import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonObjectNodeBuilder;

import pesterchum.client.Launch;
import pesterchum.client.Util;
import pesterchum.client.data.ICData;
import pesterchum.client.data.Language;
import pesterchum.client.data.Message;
import pesterchum.client.data.Settings;
import pesterchum.client.data.User;
import pesterchum.client.gui.PesterchumGUI;
import uk.co.tstableford.utilities.Log;
import uk.co.tstableford.utilities.Utilities;

public class Interface implements IncomingJson{
	private PesterchumGUI gui;
	private Connection conn;
	private Settings settings;
	private User user;
	private Language lang;
	public Interface(PesterchumGUI gui, Settings settings) throws IOException{
		this.gui = gui;
		this.conn = new Connection(this);
		this.settings = settings;
		conn.registerIncoming("message", this);
		conn.registerIncoming("admin", this);
		//load the language
		if(this.getClass().getResource("/"+settings.getString("language")+".json")!=null){
			lang = new Language("/"+settings.getString("language")+".json");
		}else if((new File(settings.getString("language")).exists())){
			lang = new Language(new File(settings.getString("language")));
		}else{
			lang = new Language();
		}
	}
	public String translate(String key){
		return lang.get(key);
	}
	public Settings getSettings(){
		return settings;
	}
	public void register(String username, String password){
		JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder()
				.withField("class", JsonNodeBuilders.aStringBuilder("admin"))
				.withField("command", JsonNodeBuilders.aStringBuilder("register"))
				.withField("username", JsonNodeBuilders.aStringBuilder(Utilities.encodeHex(username.getBytes())))
				.withField("password", JsonNodeBuilders.aStringBuilder(Utilities.encodeHex(password.getBytes())));
		conn.getConnection().write(Util.jsonToString(builder.build()));
	}
	public boolean authenticated(){
		return this.user!=null;
	}
	public void timeout(){
		gui.timeout();
	}
	@Override
	public void processIncoming(ICData data) {
		if(authenticated()){
			switch(data.getName()){
			case "message":
				try {
					Message m = new Message(data);
					gui.incomingMessage(m);
				} catch (IOException e) {
					System.err.println(e);
				}
				break;
			case "admin":
				processAdmin(data);
				break;
			default:
				Log.getInstance().error("Unknown data - "+data.getData());
			}
		}else{
			switch(data.getName()){
			case "admin":
				processAdmin(data);
				break;
			default:
				Log.getInstance().error("Unknown data - "+data.getData());
			}
		}
	}
	public void login(String username, String password){
		JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder()
				.withField("class", JsonNodeBuilders.aStringBuilder("admin"))
				.withField("command", JsonNodeBuilders.aStringBuilder("login"))
				.withField("username", JsonNodeBuilders.aStringBuilder(Utilities.encodeHex(username.getBytes())))
				.withField("password", JsonNodeBuilders.aStringBuilder(Utilities.encodeHex(password.getBytes())));
		conn.getConnection().write(Util.jsonToString(builder.build()));
	}
	public void close(){
		conn.close();
		user = null;
	}
	public User getUser(){
		return user;
	}
	public boolean connect(String host, int port){
		conn.close();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			//insomniac thread
		}
		return conn.connect(host, port);
	}
	public void sendMessage(Message message){
		conn.getConnection().write(Util.jsonToString(message.getJson()));
	}
	public void addFriend(String username){
		if(!user.hasFriend(username)){
			JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder()
					.withField("class", JsonNodeBuilders.aStringBuilder("admin"))
					.withField("command", JsonNodeBuilders.aStringBuilder("friendrequest"))
					.withField("username", JsonNodeBuilders.aStringBuilder((Utilities.encodeHex(username.getBytes()))));
			conn.getConnection().write(Util.jsonToString(builder.build()));
		}
	}
	private void processFriendResponse(ICData data){
		String username = new String(Utilities.decodeHex(data.getData().getStringValue("username")));
		boolean suc = Boolean.parseBoolean(data.getData().getStringValue("success"));
		if(suc){
			user.addFriend(username);
		}
		gui.friendRequestResponse(username, suc);
	}
	private void processAdmin(ICData data){
		switch(data.getData().getStringValue("command")){
		case "friendresponse":
			processFriendResponse(data);
			break;
		case "login":
			processLogin(data);
			break;
		case "hello":
			processHello(data);
			break;
		default:
			Log.getInstance().error("Unknown admin command - "+data.getData().getStringValue("command"));
		}
	}
	private void processHello(ICData data){
		Log.getInstance().info("Received hello, server version "+data.getData().getStringValue("version"));
		int v = -1;
		try{
			v = Integer.parseInt(data.getData().getStringValue("versionn"));
		}catch(NumberFormatException e){
			Log.getInstance().error("Version received from server not a number");
		}
		if(v>0){
			if(v>Launch.VERSION){
				gui.updateRequired(v);
			}
		}
	}
	private boolean processLogin(ICData data){
		Log.getInstance().info("Login response received");
		boolean suc = Boolean.parseBoolean(data.getData().getStringValue("success"));
		if(suc){
			this.user = new User(data.getData().getNode("user"));
		}
		gui.loginResponse(suc);
		return suc;
	}
}
