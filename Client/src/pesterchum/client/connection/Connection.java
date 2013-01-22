package pesterchum.client.connection;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.net.SocketFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import pesterchum.client.data.ICData;
import pesterchum.client.data.Incoming;

public class Connection implements Runnable{
	private BufferedReader in;
	private BufferedOutputStream out;
	private Socket socket;
	private boolean run;
	private DocumentBuilder builder;
	private String username;
	private List<String> writeBuffer;
	private Hashtable<String, Incoming> interfaces;
	private Encryption enc;
	public Connection(){
		this.username = null;
		this.interfaces = new Hashtable<String, Incoming>();
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
	public void registerIncoming(String name, Incoming inc){
		interfaces.put(name, inc);
	}
	public Incoming getIncoming(String name){
		return interfaces.get(name);
	}
	public synchronized void sendData(String data){
		writeBuffer.add(data);
	}
	public boolean encrypted(){
		if(enc!=null){
			return true;
		}
		return false;
	}
	public void setEncryption(Encryption enc){
		this.enc = enc;
	}
	private void processIncoming(String data) throws SAXException, IOException{
		if(enc!=null){
			//this we need to decrypt
			data = enc.decrypt(data);
		}
		Document doc = builder.parse(new ByteArrayInputStream(data.getBytes()));
		doc.getDocumentElement().normalize();
		String name = doc.getDocumentElement().getNodeName();
		getIncoming(name).processIncoming(new ICData(name, data));
	}
	public void setUsername(String username){
		this.username = username;
	}
	public String getUsername(){
		return this.username;
	}
	public String getSource(){
		return this.socket.getInetAddress().toString();
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
