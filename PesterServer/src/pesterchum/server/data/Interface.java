package pesterchum.server.data;

import argo.jdom.JsonArrayNodeBuilder;
import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonObjectNodeBuilder;

import pesterchum.server.Util;
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
			case "message":
				manager.sendMessage(new Message(data));
				break;
			case "admin":
				processAdmin(data);
				break;
			default:
				System.err.println("Unknown data from "+data.getSource().getConn().getSource()+" - "+data.getData());
			}
		}else{
			switch(data.getName()){
			case "admin":
				processAdmin(data);
				break;
			default:
				System.err.println("Unknown data from "+data.getSource().getConn().getSource()+" - "+data.getData());
			}
		}
	}
	private void processFriendRequest(ICData data){
		String username = new String(Utilities.decodeHex(data.getData().getStringValue("username")));
		JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder()
				.withField("class", JsonNodeBuilders.aStringBuilder("admin"))
				.withField("command", JsonNodeBuilders.aStringBuilder("friendresponse"))
				.withField("username", JsonNodeBuilders.aStringBuilder(Utilities.encodeHex(username.getBytes())));
		boolean exists = manager.getDatabase().userExists(username);
		builder.withField("success", JsonNodeBuilders.aStringBuilder(Boolean.toString(exists)));
		data.getSource().getConn().write(Util.jsonToString(builder.build()));
	}
	private void processAdmin(ICData data){
		switch(data.getData().getStringValue("command")){
		case "disconnect":
			data.getSource().close();
			break;
		case "friendrequest":
			processFriendRequest(data);
			break;
		case "login":
			processLogin(data);
			break;
		default:
			System.err.println("Unknown admin command from "+data.getSource().getConn().getSource());
		}
	}
	private void processLogin(ICData data){
		String un = data.getData().getStringValue("username");
		String pw = data.getData().getStringValue("password");
		User u = new User(un);
		manager.authenticate(u, pw);

		JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder()
				.withField("class", JsonNodeBuilders.aStringBuilder("admin"))
				.withField("command", JsonNodeBuilders.aStringBuilder("login"))
				.withField("username", JsonNodeBuilders.aStringBuilder(un))
				.withField("success", JsonNodeBuilders.aStringBuilder(Boolean.toString(u.authenticated())));
		if(u.authenticated()){
			JsonArrayNodeBuilder arr = JsonNodeBuilders.anArrayBuilder();
			for(String f: u.getFriends()){
				arr.withElement(JsonNodeBuilders.anObjectBuilder()
						.withField("username", JsonNodeBuilders.aStringBuilder(f)));
			}
			builder.withField("friends", arr);
		}
		data.getSource().getConn().write(Util.jsonToString(builder.build()));
		if(u.authenticated()){
			data.getSource().setUser(u);
			manager.registerUser(un, data.getSource());
		}
	}
}
