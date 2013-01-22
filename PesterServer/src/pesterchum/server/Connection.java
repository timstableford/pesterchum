package pesterchum.server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import pesterchum.server.data.Database;
import pesterchum.server.data.User;

public class Connection implements Runnable{
	private static final int VERSION = 1;
	private BufferedReader in;
	private BufferedOutputStream out;
	private boolean run;
	private Socket socket;
	private DocumentBuilder builder;
	private User user;
	private Encryption enc;
	private Database database;	
	public Connection(Socket socket, Database database){
		this.database = database;
		this.socket = socket;
		run = true;
		(new Thread(this)).start();
	}
	public void sendData(String data){
		if(enc!=null){
			data = enc.encrypt(data);
		}
		try {
			out.write((data+"\n").getBytes());
			out.flush();
		} catch (IOException e) {
			System.err.println("Couldn't write to "+socket.getInetAddress());
		}
	}
	private void processIncoming(String data) throws SAXException, IOException{
		if(enc!=null){
			//this we need to decrypt
			data = enc.decrypt(data);
		}
		Document doc = builder.parse(new ByteArrayInputStream(data.getBytes()));
		doc.getDocumentElement().normalize();
		String name = doc.getDocumentElement().getNodeName();
		if(this.user!=null&&this.user.authenticated()){
			//logged in
			switch(name){
			case "message":
				
				break;
			default:
				System.err.println("Incoming data unknown");
				break;
			}
		}
		//all
		switch(name){
		case "login":
			System.out.println("Login request from "+socket.getInetAddress());
			processLogin(data);
			break;
		default:
			System.err.println("Incoming data unknown");
			break;
		}
	}
	private void processLogin(String data){
		Document docin;
		try {
			docin = builder.parse(new ByteArrayInputStream(data.getBytes()));
			Element e = Util.getFirst(docin, "login");
			String un = Util.getTagValue("username", e);
			String pw = Util.getTagValue("password", e);
			User u = new User(un);
			database.authenticate(u, pw);
			Document doc = builder.newDocument();
			Element root = doc.createElement("login");
			doc.appendChild(root);
			
			Element username = doc.createElement("username");
			username.appendChild(doc.createTextNode(un));
			root.appendChild(username);
			
			Element suc = doc.createElement("success");
			suc.appendChild(doc.createTextNode(u.authenticated()+""));
			root.appendChild(suc);
			
			sendData(Util.docToString(doc));
			
			if(u.authenticated()){
				this.user = u;
			}
		} catch (SAXException | IOException e1) {
			System.err.println("Could not authenticate login for "+socket.getInetAddress());
		}
	}
	private void sendHello() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, NoSuchProviderException{
		Document doc = builder.newDocument();
		Element root = doc.createElement("hello");
		doc.appendChild(root);
		
		Element ver = doc.createElement("version");
		ver.appendChild(doc.createTextNode(VERSION+""));
		root.appendChild(ver);
		
		enc = new Encryption();
		String k = Encryption.encode(enc.getKey());
		Element key = doc.createElement("key");
		key.appendChild(doc.createTextNode(k));
		root.appendChild(key);
		
		out.write((Util.docToString(doc)+"\n").getBytes());
		out.flush();
		
	    System.out.println("Stream from "+socket.getInetAddress()+" encrypted");
	}
	@Override
	public void run() {
		System.out.println("Connection from "+socket.getInetAddress());	
		//setup document builder
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			builder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println("Couldn't setup document builder");
		}
		//setup streams
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new BufferedOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			run = false;
		}
		try {
			try {
				sendHello();
			} catch (InvalidKeyException | NoSuchAlgorithmException
					| NoSuchPaddingException | NoSuchProviderException e) {
				System.err.println("Could not send hello to "+socket.getInetAddress());
			}
		} catch (IOException e2) {
			System.err.println("Could not send hello to "+socket.getInetAddress());
		}
		while(run){
			try {
				if(in.ready()){
					String i = in.readLine();
					processIncoming(i);
				}
			} catch (IOException | SAXException e1) {
				System.err.println("Could not read from "+socket.getInetAddress());
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				//Thread can't sleep, silly insomniac
			}
		}
	}

}
