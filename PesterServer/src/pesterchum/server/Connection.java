package pesterchum.server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import pesterchum.server.data.Manager;
import pesterchum.server.data.ICData;
import pesterchum.server.data.User;

public class Connection implements Runnable{
	public static final int VERSION = 1;
	private static final long TIMEOUT = 10000;
	private BufferedReader in;
	private BufferedOutputStream out;
	private boolean run;
	private Socket socket;
	private DocumentBuilder builder;
	private User user;
	private Encryption enc;
	private Manager database;	
	private Server server;
	private long lastPing;
	private long lastPong;
	public Connection(Socket socket, Manager database, Server server){
		this.database = database;
		this.socket = socket;
		this.server = server;
		this.lastPing = System.currentTimeMillis();
		this.lastPong = System.currentTimeMillis();
		this.enc = new Encryption();
		run = true;
		(new Thread(this)).start();
	}
	public void setLastPong(long pong){
		this.lastPong = pong;
	}
	public Encryption getEncryption(){
		return this.enc;
	}
	public void sendData(String data, boolean encrypt){
		if(encrypt&&enc.secure()){
			data = enc.encryptSymmetric(data);
		}
		try {
			out.write((data+"\n").getBytes());
			out.flush();
		} catch (IOException e) {
			if(socket!=null){
				System.err.println("Couldn't write to "+socket.getInetAddress());
			}
		}
	}
	private void processIncoming(String data) throws SAXException, IOException{
		if(enc.secure()){
			//this we need to decrypt
			data = enc.decryptSymmetric(data);
		}
		Document doc = builder.parse(new ByteArrayInputStream(data.getBytes()));
		doc.getDocumentElement().normalize();
		String name = doc.getDocumentElement().getNodeName();
		database.getInterface(name).processIncoming(new ICData(name, data, this));
	}
	
	public User getUser(){
		return this.user;
	}
	public void setUser(User user){
		this.user = user;
	}
	public String getSource(){
		return socket.getInetAddress().toString();
	}
	public void disconnect(){
		if(socket!=null){
			sendData("<admin><command>disconnect</command></admin>", true);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				//Silly impatient thread
			}
			try {
				out.close();
				in.close();
				socket.close();
			} catch (IOException e) {
				//we're going to close it anyway
			}
			socket = null;
			if(this.user!=null){
				database.removeUser(this.user.getUsername());
			}
			run = false;
			server.disconnect(this);
		}
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
		lastPong = System.currentTimeMillis();
		while(run){
			//do the ping pong
			if((System.currentTimeMillis()-lastPing)>TIMEOUT/2){
				sendData("<admin><command>ping</command></admin>", true);
				lastPing = System.currentTimeMillis();
			}
			if((System.currentTimeMillis()-lastPong)>TIMEOUT){
				System.out.println("Timeout from "+this.getSource());
				this.disconnect();
			}
			try {
				if(in!=null&&in.ready()){
					String i = in.readLine();
					processIncoming(i);
				}
			} catch (IOException | SAXException e1) {
				if(socket!=null){
					System.err.println("Could not read from "+socket.getInetAddress());
				}
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				//Thread can't sleep, silly insomniac
			}
		}
	}

}
