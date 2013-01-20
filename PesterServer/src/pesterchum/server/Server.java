package pesterchum.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class Server {
	private boolean run;
	private ServerSocket server;
	private List<Connection> clients;
	public Server(int port){
		System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
		ServerSocketFactory sslserversocketfactory = createServerSocketFactory("plain");
		try {
			server = sslserversocketfactory.createServerSocket(port);
		} catch (IOException e) {
			System.err.println("Could not bind to socket at "+port);
			System.exit(-1);
		}
		clients = Collections.synchronizedList(new ArrayList<Connection>());
		run = true;
		run();
	}
	public void run(){
		while(run){
			Socket socket = null;
			try {
				socket = server.accept();
			}catch(IOException e){
				System.err.println("Could not accept connection");
			}
			if(socket!=null){
				Connection conn = new Connection(socket);
				clients.add(conn);
			}
		}
	}
	private ServerSocketFactory createServerSocketFactory(String type) {
		String ksName = "mySrvKeystore";
		char ksPass[] = "password".toCharArray();
		if("TLS".equals(type)){
			try {
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				KeyStore ks = KeyStore.getInstance("JKS");
				ks.load(new FileInputStream(ksName), ksPass);
				kmf.init(ks, ksPass);
				SSLContext sc = SSLContext.getInstance("TLS");
				sc.init(kmf.getKeyManagers(), null, null);
				return sc.getServerSocketFactory();
			} catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | CertificateException | IOException | KeyManagementException e) {
				System.err.println("Could not setup secure socket, setting up standard");
			}

		}
		return ServerSocketFactory.getDefault();
	}

}
