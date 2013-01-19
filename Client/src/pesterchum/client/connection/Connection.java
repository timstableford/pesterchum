package pesterchum.client.connection;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.LinkedList;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import pesterchum.client.gui.GUI;

public class Connection implements Runnable{
	private BufferedReader in;
	private OutputStream out;
	private Socket socket;
	private LinkedList<byte[]> writeBuffer;
	private boolean run;
	private DocumentBuilder builder;
	private GUI gui;
	public Connection(GUI gui){
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			builder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println("Couldn't setup document builder");
		}
	}
	public boolean connect(String host, int port){
		TrustManager[] trustAllCerts = new TrustManager[] { 
				new X509TrustManager() {     
					public java.security.cert.X509Certificate[] getAcceptedIssuers() { 
						return null;
					} 
					public void checkClientTrusted( 
							java.security.cert.X509Certificate[] certs, String authType) {
					} 
					public void checkServerTrusted( 
							java.security.cert.X509Certificate[] certs, String authType) {
					}
				} 
		}; 
		SocketFactory sf = null;
		try {
			SSLContext sc = SSLContext.getInstance("SSL"); 
			sc.init(null, trustAllCerts, new java.security.SecureRandom()); 
			sf = sc.getSocketFactory();	
		} catch (GeneralSecurityException e) {
			return false;
		} 
		try {
			socket = sf.createSocket(host, port);	
		} catch (IOException e) {
			return false;
		}
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = socket.getOutputStream();
		} catch (IOException e) {
			return false;
		}
		writeBuffer = new LinkedList<byte[]>();
		run = true;
		(new Thread(this)).start();
		return true;
	}
	public boolean login(String username, String password){
		Document doc = builder.newDocument();
		Element root = doc.createElement("login");
		doc.appendChild(root);
		Element un = doc.createElement("username");
		un.appendChild(doc.createTextNode(username));
		
		return true;
	}
	private synchronized void sendData(byte[] data){
		writeBuffer.add(data);
	}
	public void sendMessage(Message message){
		sendData((message.getXML()+"\n").getBytes());
	}
	private void processIncoming(String data) throws SAXException, IOException{
		Document doc = builder.parse(new ByteArrayInputStream(data.getBytes()));
		doc.getDocumentElement().normalize();
		String name = doc.getDocumentElement().getNodeName();
		switch(name){
		case "message":
			Message m = new Message(data);
			gui.incomingMessage(m);
			break;
		default:
			System.err.println("Incoming data unknown");
			break;
		}
	}
	@Override
	public void run(){
		while(run){
			if(writeBuffer.size()>0){
				try {
					out.write(writeBuffer.remove());
				} catch (IOException e) {
					System.err.println("Error writing to server");
				}
			}
			try {
				if(in.ready()){
					processIncoming(in.readLine());
				}
			} catch (IOException | SAXException e) {
				System.err.println("Error reading from server");
			}
		}
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			//It doesn't really matter if we ignore this
		}
	}
}
