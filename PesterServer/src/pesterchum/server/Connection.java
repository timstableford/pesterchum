package pesterchum.server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.xml.sax.SAXException;

import argo.jdom.JdomParser;
import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonObjectNodeBuilder;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

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
	private User user;
	private Encryption enc;
	private Manager database;	
	private Server server;
	private long lastPing;
	private long lastPong;
	private final JdomParser parser = new JdomParser();
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
			if(!data.endsWith("\n")){
				data = data + "\n";
			}
			out.write(data.getBytes());
			out.flush();
		} catch (IOException e) {
			if(socket!=null){
				System.err.println("Couldn't write to "+socket.getInetAddress());
			}
		}
	}
	private void processIncoming(String data) throws SAXException, IOException{
		if(enc.secure()){
			data = enc.decryptSymmetric(data);
		}
		try {
			JsonRootNode node = parser.parse(data);
			String name = node.getStringValue("class");
			database.getInterface(name).processIncoming(new ICData(name, node, this));
		} catch (InvalidSyntaxException e) {
			System.err.println("Error parsing incoming");
		}
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
			JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder()
					.withField("class", JsonNodeBuilders.aStringBuilder("admin"))
					.withField("command", JsonNodeBuilders.aStringBuilder("disconnect"));
			sendData(Util.jsonToString(builder.build()), true);
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
				JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder()
						.withField("class", JsonNodeBuilders.aStringBuilder("admin"))
						.withField("command", JsonNodeBuilders.aStringBuilder("ping"));
				sendData(Util.jsonToString(builder.build()), true);
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
