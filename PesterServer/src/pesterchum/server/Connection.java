package pesterchum.server;

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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import pesterchum.server.data.User;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Connection implements Runnable{
	private static final int VERSION = 1;
	private BufferedReader in;
	private OutputStream out;
	private boolean run;
	private Socket socket;
	private DocumentBuilder builder;
	private User user;
	private SecretKeySpec sks;
	private Cipher enc, denc;
	public Connection(Socket socket){
		this.socket = socket;
		run = true;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			builder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println("Couldn't setup document builder");
		}
		(new Thread(this)).start();
	}
	public void write(String data){
		if(enc!=null){
			//then we need to encrypt
			try {
				BASE64Encoder e = new BASE64Encoder();
				data = URLEncoder.encode(new String(e.encode(enc.doFinal(data.getBytes()))), "UTF-8");
			} catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
				System.err.println("Could not encrypt and encode for "+socket.getInetAddress());
			}
		}
		byte[] o = (data+"\n").getBytes();
		try {
			out.write(o);
		} catch (IOException e) {
			System.err.println("Couldn't write to "+socket.getInetAddress());
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
				System.err.println("Decoding and decryption failed for "+socket.getInetAddress());
			}
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
			//TODO 
			//processLogin(data);
			System.out.println("Login request from "+socket.getInetAddress());
			break;
		default:
			System.err.println("Incoming data unknown");
			break;
		}
	}
	private void sendHello() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, NoSuchProviderException{
		Document doc = builder.newDocument();
		Element root = doc.createElement("hello");
		doc.appendChild(root);
		
		Element ver = doc.createElement("version");
		ver.appendChild(doc.createTextNode(VERSION+""));
		root.appendChild(ver);
		byte[] ke = (new KeyGen()).getKey().getEncoded();
		String k = URLEncoder.encode(new String(ke),"UTF-8");
		Element key = doc.createElement("key");
		key.appendChild(doc.createTextNode(k));
		root.appendChild(key);
		
		out.write((Util.docToString(doc)+"\n").getBytes());
		
		sks = new SecretKeySpec(ke, "AES");
		enc = Cipher.getInstance("AES");
	    enc.init(Cipher.ENCRYPT_MODE, sks);
	    denc = Cipher.getInstance("AES");
	    denc.init(Cipher.DECRYPT_MODE, sks);
	    System.out.println("Stream from "+socket.getInetAddress()+" encrypted");
	}
	@Override
	public void run() {
		System.out.println("Connection from "+socket.getInetAddress());	
		//setup streams
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = socket.getOutputStream();
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
