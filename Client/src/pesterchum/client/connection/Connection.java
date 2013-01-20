package pesterchum.client.connection;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pesterchum.client.gui.GUI;

public class Connection implements Runnable{
	private BufferedReader in;
	private OutputStream out;
	private Socket socket;
	private boolean run;
	private DocumentBuilder builder;
	private GUI gui;
	private String username;
	public Connection(GUI gui){
		System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
		this.username = null;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			builder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println("Couldn't setup document builder");
		}
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
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			CharArrayWriter writer = new CharArrayWriter();
			StreamResult result = new StreamResult(writer);
			transformer.transform(source, result);
			sendData(writer.toString());
		} catch (TransformerException e) {
			return false;
		}
		return true;
	}
	private synchronized void sendData(String data){
		byte[] toWrite = (data+"\n").getBytes();
		try {
			out.write(toWrite);
		} catch (IOException e) {
			System.err.println("Error writing to server");
		}
	}
	public void sendMessage(Message message){
		sendData(message.getXML());
	}
	private void processIncoming(String data) throws SAXException, IOException{
		Document doc = builder.parse(new ByteArrayInputStream(data.getBytes()));
		doc.getDocumentElement().normalize();
		String name = doc.getDocumentElement().getNodeName();
		if(this.username!=null){
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
			processLogin(data);
		}else{
			System.err.println("Unexpected data "+data);
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
				String un = getTagValue("username", eElement);
				suc = Boolean.parseBoolean(getTagValue("success", eElement));
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
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				//It doesn't really matter if we ignore this
			}
		}
	}
	private String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		Node nValue = (Node) nlList.item(0);
		return nValue.getNodeValue();
	}
	private SocketFactory getSocketFactory(String type){
		if("TLS".equals(type)){
			X509TrustManager trustManager = new X509TrustManager() {
				@Override
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] chain,
						String authType) throws CertificateException {
					//Do nothing
				}
				@Override
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] chain,
						String authType) throws CertificateException {
					//Do nothing
				}
				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return new java.security.cert.X509Certificate[0];
				}
			};
			try {
				SSLContext sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, new TrustManager[] { trustManager }, new SecureRandom());
				return sslContext.getSocketFactory();
			} catch (KeyManagementException | NoSuchAlgorithmException e) {
				System.err.println("Could not setup TLS connection, going plain");
			}
		}
		return SocketFactory.getDefault();
	}
}
