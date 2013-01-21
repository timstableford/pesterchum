package pesterchum.client.connection;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
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
import pesterchum.client.gui.GUI;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Connection implements Runnable{
	private BufferedReader in;
	private OutputStream out;
	private Socket socket;
	private boolean run;
	private DocumentBuilder builder;
	private GUI gui;
	private String username;
	private SecretKeySpec sks;
	private static final int VERSION = 1;
	private List<byte[]> writeBuffer;
	private Cipher enc, denc;
	public Connection(GUI gui){
		System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
		this.username = null;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			builder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println("Couldn't setup document builder");
		}
		writeBuffer = Collections.synchronizedList(new LinkedList<byte[]>());
	}
	public boolean connect(String host, int port){
		SocketFactory sf = getSocketFactory("plain");
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
		byte[] toWrite = (data).getBytes();
		writeBuffer.add(toWrite);
	}
	public void sendMessage(Message message){
		sendData(message.getXML());
	}
	private void processHello(String data) throws SAXException, IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, NoSuchProviderException{
		Document doc = builder.parse(new ByteArrayInputStream(data.getBytes()));
		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("hello");
		Node nNode = nList.item(0);
		String key = null;
		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			Element eElement = (Element) nNode;
			int ver = Integer.parseInt(Util.getTagValue("version", eElement));
			key = Util.getTagValue("key", eElement);
			key = URLDecoder.decode(key, "UTF-8");
			sks = new SecretKeySpec(key.getBytes(), "AES");
		}
		if(key!=null){
			enc = Cipher.getInstance("AES");
		    enc.init(Cipher.ENCRYPT_MODE, sks);
		    denc = Cipher.getInstance("AES");
		    denc.init(Cipher.DECRYPT_MODE, sks);
		    System.out.println("Connection encrypted");
		}
	}
	private void processIncoming(String data) throws SAXException, IOException{
		if(denc!=null){
			//this we need to decrypt
			try {
				BASE64Decoder d = new BASE64Decoder();
				byte[] a = d.decodeBuffer(URLDecoder.decode(data, "UTF-8"));
				data = new String(denc.doFinal(a));
			} catch (IllegalBlockSizeException | BadPaddingException e) {
				System.err.println("Decoding and decryption failed");
			}
		}
		Document doc = builder.parse(new ByteArrayInputStream(data.getBytes()));
		doc.getDocumentElement().normalize();
		String name = doc.getDocumentElement().getNodeName();
		if(this.username!=null){
			//logged in
			switch(name){
			case "message":
				Message m = new Message(data);
				gui.incomingMessage(m);
				break;
			default:
				System.err.println("Incoming data unknown");
				break;
			}
		}else if(name=="login"){
			//not logged in
			processLogin(data);
		}
		//all
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
					byte[] tw = null;
					try {
						BASE64Encoder e = new BASE64Encoder();
						tw = (URLEncoder.encode(new String(e.encode(enc.doFinal(writeBuffer.get(0)))), "UTF-8")+"\n").getBytes();
					} catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
						System.err.println("Could not encrypt and encode for "+socket.getInetAddress());
					}
					out.write(tw);
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
	private SocketFactory getSocketFactory(String type){
		return SocketFactory.getDefault();
	}
}
