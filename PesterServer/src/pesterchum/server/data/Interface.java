package pesterchum.server.data;

import java.awt.Color;
import java.io.IOException;

import argo.jdom.JsonNode;
import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonObjectNodeBuilder;

import pesterchum.server.Util;
import uk.co.tstableford.utilities.Log;
import uk.co.tstableford.utilities.Utilities;

public class Interface implements IncomingJson{
	private Manager manager;
	public Interface(Manager manager){
		this.manager = manager;
	}
	private boolean authenticated(ICData data){
		if(data.getSource().getUser()!=null&&data.getSource().getUser().authenticated()){
			return true;
		}
		return false;
	}
	@Override
	public void processIncoming(ICData data) {
		if(authenticated(data)){
			switch(data.getName()){
			case "packet":
				try {
					Packet p = Manager.parsePacket(data);
					manager.sendPacket(p);
				} catch (IOException e1) {
					Log.getInstance().error("Could not parse data packet");
				}
				break;
			case "admin":
				processAdmin(data);
				break;
			default:
				Log.getInstance().error("Unknown data from "+data.getSource().getConn().getSource()+" - "+data.getData());
			}
		}else{
			switch(data.getName()){
			case "admin":
				processAdmin(data);
				break;
			default:
				Log.getInstance().error("Unknown data from "+data.getSource().getConn().getSource()+" - "+data.getData());
			}
		}
	}
	private void processAdmin(ICData data){
		switch(data.getData().getStringValue("command")){
		case "disconnect":
			data.getSource().close();
			break;
		case "friendrequest":
			processFriendRequest(data);
			break;
		case "setcolor":
			setColor(data);
			break;
		case "login":
			processLogin(data);
			break;
		case "register":
			processRegistration(data);
			break;
		default:
			Log.getInstance().error("Unknown admin command from "+data.getSource().getConn().getSource());
			Log.getInstance().debug(data.getData().toString(), 3);
		}
	}
	private void setColor(ICData data){
		JsonNode n = data.getData().getNode("color");
		Color c = new Color(
				Integer.parseInt(n.getStringValue("red")),
				Integer.parseInt(n.getStringValue("green")),
				Integer.parseInt(n.getStringValue("blue"))
				);
		data.getSource().getUser().setColor(c);
		manager.getDatabase().saveUser(data.getSource().getUser());
	}
	private void processFriendRequest(ICData data){
		String username = new String(Utilities.decodeHex(data.getData().getStringValue("username")));
		JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder()
				.withField("class", JsonNodeBuilders.aStringBuilder("admin"))
				.withField("command", JsonNodeBuilders.aStringBuilder("friendresponse"))
				.withField("username", JsonNodeBuilders.aStringBuilder(Utilities.encodeHex(username.getBytes())));
		boolean exists = manager.getDatabase().userExists(username);
		if(exists){
			if(data.getSource().getUser().hasFriend(username)){
				exists = false;
			}else{
				data.getSource().getUser().addFriend(username);
				manager.getDatabase().saveUser(data.getSource().getUser());
			}
		}
		builder.withField("success", JsonNodeBuilders.aStringBuilder(Boolean.toString(exists)));
		data.getSource().getConn().write(Util.jsonToString(builder.build()));
	}
	private void processRegistration(ICData data){
		String un = new String(Utilities.decodeHex(data.getData().getStringValue("username")));
		String pw = new String(Utilities.decodeHex(data.getData().getStringValue("password")));
		User u = new User(un);
		manager.register(u, pw);

		JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder()
				.withField("class", JsonNodeBuilders.aStringBuilder("admin"))
				.withField("command", JsonNodeBuilders.aStringBuilder("login"))
				.withField("success", JsonNodeBuilders.aStringBuilder(Boolean.toString(u.authenticated())));
		if(u.authenticated()){
			builder.withField("user", u.getJson());
		}
		data.getSource().getConn().write(Util.jsonToString(builder.build()));
		if(u.authenticated()){
			data.getSource().setUser(u);
			manager.registerUser(un, data.getSource());
		}
	}
	private void processLogin(ICData data){
		String un = new String(Utilities.decodeHex(data.getData().getStringValue("username")));
		String pw = new String(Utilities.decodeHex(data.getData().getStringValue("password")));
		User u = new User(un);
		manager.authenticate(u, pw);

		JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder()
				.withField("class", JsonNodeBuilders.aStringBuilder("admin"))
				.withField("command", JsonNodeBuilders.aStringBuilder("login"))
				.withField("success", JsonNodeBuilders.aStringBuilder(Boolean.toString(u.authenticated())));
		if(u.authenticated()){
			builder.withField("user", u.getJson());
			data.getSource().setUser(u);
		}
		data.getSource().getConn().write(Util.jsonToString(builder.build()));
		if(u.authenticated()){
			manager.registerUser(un, data.getSource());
		}
	}
}
