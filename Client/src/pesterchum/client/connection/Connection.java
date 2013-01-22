package pesterchum.client.connection;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.NoSuchPaddingException;
import javax.net.SocketFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pesterchum.client.Util;
import pesterchum.client.data.Message;
import pesterchum.client.gui.GUI;

public class Connection implements Runnable{
	private BufferedReader in;
	private BufferedOutputStream out;
	private Socket socket;
	private boolean run;
	private DocumentBuilder builder;
	private GUI gui;
	private String username;
	private static final int VERSION = 1;
	private List<String> writeBuffer;
	private Encryption enc;
	public Connection(GUI gui){
		this.username = null;
		this.gui = gui;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			builder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println("Couldn't setup document builder");
		}
		writeBuffer = Collections.synchronizedList(new LinkedList<String>());
	}
	public boolean connect(String host, int port){
		try {
			socket = getSocketFactory().createSocket(host, port);	
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new BufferedOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			return false;
		}
		run = true;
		(new Thread(this)).start();
		return true;
	}
	public boolean login(String username, String password){
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
		sendData(Util.docToString(doc));
		return true;
	}
	private synchronized void sendData(String data){
		writeBuffer.add(data);
	}
	public void sendMessage(Message message){
		sendData(message.getXML());
	}
	public boolean encrypted(){
		if(enc!=null){
			return true;
		}
		return false;
	}
	private void processHello(String data) throws SAXException, IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, NoSuchProviderException{
		Document doc = builder.parse(new ByteArrayInputStream(data.getBytes()));
		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("hello");
		Node nNode = nList.item(0);
		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			Element eElement = (Element) nNode;
			int ver = Integer.parseInt(Util.getTagValue("version", eElement));
			byte[] key = Encryption.decode(Util.getTagValue("key", eElement));
			enc = new Encryption(key);
			if(ver!=VERSION){
				gui.versionMismatch(VERSION, ver);
			}
		}
	}
	private void processIncoming(String data) throws SAXException, IOException{
		if(enc!=null){
			//this we need to decrypt
			data = enc.decrypt(data);
		}
		boolean dealt = false;
		Document doc = builder.parse(new ByteArrayInputStream(data.getBytes()));
		doc.getDocumentElement().normalize();
		String name = doc.getDocumentElement().getNodeName();
		if(this.username!=null){
			//logged in
			switch(name){
			case "message":
				Message m = new Message(data);
				gui.incomingMessage(m);
				dealt = true;
				break;
			default:
				break;
			}
		}else if(name=="login"&&!dealt){
			//not logged in
			processLogin(data);
			dealt = true;
		}
		//all
		if(!dealt)
			switch(name){
			case "hello":
				try {
					processHello(data);
				} catch (InvalidKeyException | NoSuchAlgorithmException
						| NoSuchPaddingException | NoSuchProviderException e) {
					System.err.println("Could not process hello message");
				}
				break;
			default:
				System.err.println("Incoming data unknown");
				break;
			}
	}
	private boolean processLogin(String data){
		Document doc = null;
		boolean suc = false;
		try {
			doc = builder.parse(new ByteArrayInputStream(data.getBytes()));
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
					this.username = un;
				}
			}
		}
		gui.loginResponse(suc);
		return suc;
	}
	@Override
	public void run(){
		while(run){
			try {
				if(in.ready()){
					processIncoming(in.readLine());
				}
			} catch (IOException | SAXException e) {
				System.err.println("Error reading from server");
			}
			if(enc!=null&&writeBuffer.size()>0){
				try {
					String d = enc.encrypt(writeBuffer.get(0))+"\n";
					out.write(d.getBytes());
					out.flush();
				} catch (IOException e) {
					System.err.println("Could not send "+new String(writeBuffer.get(0)));
				}
				writeBuffer.remove(0);
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				//It doesn't really matter if we ignore this
			}
		}
	}
	private SocketFactory getSocketFactory(){
		return SocketFactory.getDefault();
	}
}
