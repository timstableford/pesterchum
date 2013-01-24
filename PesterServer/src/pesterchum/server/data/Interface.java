package pesterchum.server.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import pesterchum.server.Connection;
import pesterchum.server.Encryption;
import pesterchum.server.Util;

public class Interface implements Incoming{
	private Manager manager;
	private DocumentBuilder builder;
	public Interface(Manager manager){
		this.manager = manager;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			builder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println("Couldn't setup document builder");
		}
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
			case "friendrequest":
				processFriendRequest(data);
				break;
			default:
				System.err.println("Unknown data from "+data.getSource().getSource()+" - "+data.getData());
			}
		}else{
			switch(data.getName()){
			case "publickey":
				processHello(data);
				break;
			case "login":
				processLogin(data);
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
			data.getSource().getEncryption().initAsymmetric(data.getData());
			sendHello(data);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException
				| ParserConfigurationException | SAXException | IOException 
				| InvalidKeyException | NoSuchPaddingException | NoSuchProviderException e) {
			System.err.println("Could not initialise public key");
		}
	}
	private void sendHello(ICData data) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, NoSuchProviderException{
		Document doc = builder.newDocument();
		Element root = doc.createElement("hello");
		doc.appendChild(root);

		Element ver = doc.createElement("version");
		ver.appendChild(doc.createTextNode(Connection.VERSION+""));
		root.appendChild(ver);
		data.getSource().getEncryption().initSymmetric();
		String k = Encryption.encode(data.getSource().getEncryption().getKey());
		Element key = doc.createElement("key");
		key.appendChild(doc.createTextNode(k));
		root.appendChild(key);
		
		data.getSource().sendData(data.getSource().getEncryption().encryptAsymmetric(Util.docToString(doc).getBytes()), false);
		
	    System.out.println("Stream from "+data.getSource().getSource()+" encrypted");
	}
	private void processFriendRequest(ICData data){
		try {
			Document doc = builder.parse(new ByteArrayInputStream(data.getData().getBytes()));
			String name = new String(Encryption.decode(doc.getDocumentElement().getNodeValue()));
			data.getSource().sendData("<friendrequest><name>"+doc.getDocumentElement().getNodeValue()+"</name>"
					+"<success>"+manager.getDatabase().userExists(new User(name))+"</success></friendrequest>", true);
		} catch (SAXException | IOException e) {
			//we'll just ignore it, if the client never gets a response it doesnt matter
		}
	}
	private void processAdmin(ICData data){
		try {
			Document doc = builder.parse(new ByteArrayInputStream(data.getData().getBytes()));
			Element e = Util.getFirst(doc, "admin");
			switch(Util.getTagValue("command", e)){
			case "disconnect":
				data.getSource().disconnect();
				break;
			case "pong":
				data.getSource().setLastPong(System.currentTimeMillis());
				break;
			default:
				System.err.println("Unknown admin command from "+data.getSource().getSource());
			}
		} catch (SAXException | IOException e) {
			System.err.println("Could not process request from "+data.getSource().getSource());
		}
	}
	private void processLogin(ICData data){
		Document docin;
		try {
			docin = builder.parse(new ByteArrayInputStream(data.getData().getBytes()));
			Element e = Util.getFirst(docin, "login");
			String un = Util.getTagValue("username", e);
			String pw = Util.getTagValue("password", e);
			User u = new User(un);
			manager.authenticate(u, pw);
			Document doc = builder.newDocument();
			Element root = doc.createElement("login");
			doc.appendChild(root);
			
			Element username = doc.createElement("username");
			username.appendChild(doc.createTextNode(un));
			root.appendChild(username);
			
			Element suc = doc.createElement("success");
			suc.appendChild(doc.createTextNode(u.authenticated()+""));
			root.appendChild(suc);
			
			Element friends = doc.createElement("friends");
			root.appendChild(friends);
			for(int i=0; i<u.getFriends().size(); i++){
				String fn = u.getFriends().get(i);
				Element friend = doc.createElement("friend");
				friend.appendChild(doc.createTextNode(fn));
				friends.appendChild(friend);
			}
			
			data.getSource().sendData(Util.docToString(doc), true);
			
			if(u.authenticated()){
				data.getSource().setUser(u);
				manager.registerUser(un, data.getSource());
			}
		} catch (SAXException | IOException e1) {
			System.err.println("Could not authenticate login for "+data.getSource().getSource());
		}
	}
}
