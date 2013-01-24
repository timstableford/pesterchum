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
		System.out.println("Connecting to "+host+" on port "+port);
		writeBuffer = Collections.synchronizedList(new LinkedList<String>());
		try {
			socket = getSocketFactory().createSocket(host, port);	
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new BufferedOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			return false;
		}
		enc = new Encryption();
		run = true;
		(new Thread(this)).start();
		return true;
	}
	public void registerIncoming(String name, Incoming inc){
		interfaces.put(name, inc);
	}
	public Encryption getEncryption(){
		return this.enc;
	}
	public void disconnect(){
		if(socket!=null){
			sendData("<admin><command>disconnect</command></admin>");
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
			//enc.reset();
			socket = null;
			username = null;
			run = false;
		}
	}
	public Incoming getIncoming(String name){
		return interfaces.get(name);
	}
	public synchronized void sendData(String data){
		writeBuffer.add(data);
	}
	public boolean encrypted(){
		return enc.secure()==Secure.YES;
	}
	private void processIncoming(String data) throws SAXException, IOException{
		switch(enc.secure()){
		case PUBLICKEY:
			data = new String(enc.decryptAsymmetric(data));
			break;
		case YES:
			data = new String(enc.decryptSymmetric(data));
			break;
		case NO:
			break;
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
		sendHello();
		while(run){
			try {
				if(in!=null&&in.ready()){
					processIncoming(in.readLine());
				}
			} catch (IOException | SAXException e) {
				System.err.println("Error reading from server");
				break; //going to assume lost connection and break loop
			}
			if(out!=null&&enc!=null&&enc.secure()==Secure.YES&&writeBuffer.size()>0){
				try {
					String d = enc.encryptSymmetric(writeBuffer.get(0).getBytes())+"\n";
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
	private void sendHello(){
		try {
			out.write((enc.getPublicKeyXML()+"\n").getBytes());
			System.out.println("Sending public key");
			out.flush();
		} catch (IOException e) {
			System.err.println("Could not send hello to server");
		}
		
	}
}
