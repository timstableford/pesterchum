package pesterchum.client.connection;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pesterchum.client.Util;
import pesterchum.client.data.ICData;
import pesterchum.client.data.Incoming;
import pesterchum.client.data.Message;
import pesterchum.client.gui.GUI;

public class Interface implements Incoming{
	private static final long TIMEOUT = 10000;
	private static final int VERSION = 1;
	private DocumentBuilder builder;
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
		conn.registerIncoming("login", this);
		conn.registerIncoming("message", this);
		conn.registerIncoming("admin", this);
		conn.registerIncoming("friendrequest", this);
		friends = new LinkedList<String>();
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			builder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println("Couldn't setup document builder");
		}
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
			case "friendrequest":
				processFriendRequest(data);
				break;
			default:
				System.err.println("Unknown data from - "+data.getData());
			}
		}else{
			switch(data.getName()){
			case "login":
				processLogin(data);
				break;
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
		//Create the document
		Document doc = builder.newDocument();
		Element root = doc.createElement("login");
		doc.appendChild(root);
		//Add the username element
		Element un = doc.createElement("username");
		un.appendChild(doc.createTextNode(username));
		root.appendChild(un);
		//Add the password element
		Element pw = doc.createElement("password");
		pw.appendChild(doc.createTextNode(password));
		root.appendChild(pw);
		//Magical code to turn it into a string and send it
		conn.sendData(Util.docToString(doc));
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
		conn.sendData(message.getXML());
	}
	public void addFriend(String username){
		conn.sendData("<friendrequest>"+Encryption.encode(username.getBytes())+"</friendrequest>");
	}
	private void processFriendRequest(ICData data){
		try {
			Document doc = builder.parse(new ByteArrayInputStream(data.getData().getBytes()));
			Element e = Util.getFirst(doc, "friendrequest");
			String username = Util.getTagValue("name", e);
			boolean suc = Boolean.parseBoolean(Util.getTagValue("success", e));
			gui.friendRequestResponse(username, suc);
		} catch (SAXException | IOException e) {
			//we'll just ignore this
		}
	}
	private void processAdmin(ICData data){
		try {
			Document doc = builder.parse(new ByteArrayInputStream(data.getData().getBytes()));
			Element e = Util.getFirst(doc, "admin");
			switch(Util.getTagValue("command", e)){
			case "disconnect":
				conn.disconnect();
				break;
			case "ping":
				conn.sendData("<admin><command>pong</command></admin>");
				lastPing = System.currentTimeMillis();
				break;
			default:
				System.err.println("Unknown admin command");
			}
		} catch (SAXException | IOException e) {
			System.err.println("Could not process admin request");
		}
	}
	private boolean processLogin(ICData data){
		Document doc = null;
		boolean suc = false;
		try {
			doc = builder.parse(new ByteArrayInputStream(data.getData().getBytes()));
		} catch (SAXException | IOException e) {}
		if(doc!=null){
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("login");
			Node nNode = nList.item(0);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				String un = Util.getTagValue("username", eElement);
				suc = Boolean.parseBoolean(Util.getTagValue("success", eElement));
				if(suc){
					conn.setUsername(un);
					NodeList list = eElement.getElementsByTagName("friends");
					for(int i=0; i<list.getLength(); i++){
						Node node = list.item(i);
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							Element e = (Element) node;
							friends.add(Util.getTagValue("friend", e));
						}
					}
				}
			}
		}
		gui.loginResponse(suc);
		return suc;
	}
	private void processHello(ICData data){
		Document doc;
		try {
			doc = builder.parse(new ByteArrayInputStream(data.getData().getBytes()));
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("hello");
			Node nNode = nList.item(0);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				int ver = Integer.parseInt(Util.getTagValue("version", eElement));
				byte[] key = Encryption.decode(Util.getTagValue("key", eElement));
				conn.getEncryption().initSymmetric(key);
				if(ver!=VERSION){
					gui.versionMismatch(VERSION, ver);
				}
				System.out.println("Received symmetric key from server");
			}
		} catch (SAXException | IOException e) {
			System.err.println("Could not process hello message from server");
		}
	}
}
