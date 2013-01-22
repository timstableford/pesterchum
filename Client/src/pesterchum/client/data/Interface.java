package pesterchum.client.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pesterchum.client.Util;
import pesterchum.client.connection.Connection;
import pesterchum.client.connection.Encryption;
import pesterchum.client.gui.GUI;

public class Interface implements Incoming{
	private static final long TIMEOUT = 10000;
	private static final int VERSION = 1;
	private DocumentBuilder builder;
	private GUI gui;
	private Connection conn;
	private long lastPing;
	public Interface(GUI gui){
		this.gui = gui;
		gui.setInterface(this);
		this.conn = new Connection();
		conn.registerIncoming("hello", this);
		conn.registerIncoming("login", this);
		conn.registerIncoming("message", this);
		conn.registerIncoming("admin", this);
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			builder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println("Couldn't setup document builder");
		}
		lastPing = System.currentTimeMillis();
	}
	public boolean authenticated(){
		if(conn.getUsername()!=null){
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
		return conn.connect(host, port);
	}
	public void sendMessage(Message message){
		conn.sendData(message.getXML());
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
				conn.setEncryption(new Encryption(key));
				if(ver!=VERSION){
					gui.versionMismatch(VERSION, ver);
				}
			}
		} catch (SAXException | IOException e) {
			System.err.println("Could not process hello message from server");
		}
	}
}
