package pesterchum.client.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.LinkedList;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Connection {
	private BufferedReader in;
	private OutputStream out;
	private Socket socket;
	private LinkedList<byte[]> writeBuffer;
	public Connection(){
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
		return true;
	}
	public boolean login(String username, String password){

		return true;
	}
	private synchronized void sendData(byte[] data){
		writeBuffer.add(data);
	}
	public void sendMessage(Message message){
		sendData((message.getXML()+"\n").getBytes());
	}
}
