package pesterchum.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class Server {
	private boolean run;
	private SSLServerSocket server;
	private List<Connection> clients;
	public Server(int port){
		System.setProperty("javax.net.ssl.keyStore", "mySrvKeystore");
		System.setProperty("javax.net.ssl.keyStorePassword", "password");
		System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
		System.setProperty("javax.net.ssl.trustStore", "mySrvKeystore"); 
		SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		try {
			server = (SSLServerSocket) sslserversocketfactory.createServerSocket(port);
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
			SSLSocket socket = null;
			try {
				socket = (SSLSocket)server.accept();
			}catch(IOException e){
				System.err.println("Could not accept connection");
			}
			if(socket!=null){
				Connection conn = new Connection(socket);
				clients.add(conn);
				(new Thread(conn)).start();
			}
		}
	}

}
