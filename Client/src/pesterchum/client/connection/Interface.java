package pesterchum.client.connection;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import argo.jdom.JsonNode;
import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonObjectNodeBuilder;

import pesterchum.client.PesterchumGUI;
import pesterchum.client.Util;
import pesterchum.client.data.ICData;
import pesterchum.client.data.IncomingJson;
import pesterchum.client.data.Language;
import pesterchum.client.data.Message;
import pesterchum.client.data.Settings;
import uk.co.tstableford.utilities.Log;
import uk.co.tstableford.utilities.Utilities;

public class Interface implements IncomingJson{
	private PesterchumGUI gui;
	private Connection conn;
	private Settings settings;
	private LinkedList<String> friends;
	private Log log;
	private Language lang;
	public Interface(PesterchumGUI gui, Settings settings, Log log) throws IOException{
		this.gui = gui;
		this.conn = new Connection(this, log);
		this.log = log;
		this.settings = settings;
		conn.registerIncoming("message", this);
		conn.registerIncoming("admin", this);
		friends = new LinkedList<String>();
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
	public void register(String u, String p){
		//TODO
	}
	public boolean authenticated(){
		if(conn.getUsername()!=null){
			return true;
		}
		return false;
	}
	public void timeout(){
		gui.timeout();
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
				log.error("Unknown data from - "+data.getData());
			}
		}else{
			switch(data.getName()){
			case "admin":
				processAdmin(data);
				break;
			default:
				log.error("Unknown data from - "+data.getData());
			}
		}
	}
	public void login(String username, String password){
		JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder()
				.withField("class", JsonNodeBuilders.aStringBuilder("admin"))
				.withField("command", JsonNodeBuilders.aStringBuilder("login"))
				.withField("username", JsonNodeBuilders.aStringBuilder(username))
				.withField("password", JsonNodeBuilders.aStringBuilder(password));
		conn.getConnection().write(Util.jsonToString(builder.build()));
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
		JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder()
				.withField("class", JsonNodeBuilders.aStringBuilder("admin"))
				.withField("command", JsonNodeBuilders.aStringBuilder("friendrequest"))
				.withField("username", JsonNodeBuilders.aStringBuilder((Utilities.encodeHex(username.getBytes()))));
		conn.getConnection().write(Util.jsonToString(builder.build()));
	}
	private void processFriendResponse(ICData data){
		String username = new String(Utilities.decodeHex(data.getData().getStringValue("username")));
		boolean suc = Boolean.parseBoolean(data.getData().getStringValue("success"));
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
		default:
			log.error("Unknown admin command");
		}
	}
	private boolean processLogin(ICData data){
		log.info("Login response received");
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
}
