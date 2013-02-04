package pesterchum.server.data;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import argo.jdom.JsonArrayNodeBuilder;
import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonObjectNodeBuilder;

import pesterchum.server.Connection;
import pesterchum.server.Encryption;
import pesterchum.server.Util;

public class Interface implements Incoming{
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
				System.err.println("Unknown data from "+data.getSource().getSource()+" - "+data.getData());
			}
		}else{
			switch(data.getName()){
			case "publickey":
				processHello(data);
				break;
			case "admin":
				processAdmin(data);
				break;
			default:
				System.err.println("Unknown data from "+data.getSource().getSource()+" - "+data.getData());
			}
		}
	}
	private void processHello(ICData data){
		try {
			data.getSource().getEncryption().initAsymmetric(data);
			sendHello(data);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException
				| ParserConfigurationException | SAXException | IOException 
				| InvalidKeyException | NoSuchPaddingException | NoSuchProviderException e) {
			System.err.println("Could not initialise public key");
		}
	}
	private void sendHello(ICData data) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, NoSuchProviderException{
		data.getSource().getEncryption().initSymmetric();
		JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder()
				.withField("class", JsonNodeBuilders.aStringBuilder("hello"))
				.withField("version", JsonNodeBuilders.aStringBuilder(Integer.toString(Connection.VERSION)))
				.withField("key", JsonNodeBuilders.aStringBuilder(Encryption.encode(
						data.getSource().getEncryption().getKey())));
		Encryption enc = data.getSource().getEncryption();
		data.getSource().sendData(enc.encryptAsymmetric(Util.jsonToString(builder.build()).getBytes()), false);
	    System.out.println("Stream from "+data.getSource().getSource()+" encrypted");
	}
	private void processFriendRequest(ICData data){
		String username = data.getData().getStringValue("username");
		JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder()
				.withField("class", JsonNodeBuilders.aStringBuilder("admin"))
				.withField("command", JsonNodeBuilders.aStringBuilder("friendresponse"))
				.withField("username", JsonNodeBuilders.aStringBuilder(username));
		boolean exists = manager.getDatabase().userExists(new User(username));
		builder.withField("success", JsonNodeBuilders.aStringBuilder(Boolean.toString(exists)));
		data.getSource().sendData(Util.jsonToString(builder.build()), true);
	}
	private void processAdmin(ICData data){
		switch(data.getData().getStringValue("command")){
		case "disconnect":
			data.getSource().disconnect();
			break;
		case "pong":
			data.getSource().setLastPong(System.currentTimeMillis());
			break;
		case "friendrequest":
			processFriendRequest(data);
			break;
		case "login":
			processLogin(data);
			break;
		default:
			System.err.println("Unknown admin command from "+data.getSource().getSource());
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
		data.getSource().sendData(Util.jsonToString(builder.build()), true);
		if(u.authenticated()){
			data.getSource().setUser(u);
			manager.registerUser(un, data.getSource());
		}
	}
}
