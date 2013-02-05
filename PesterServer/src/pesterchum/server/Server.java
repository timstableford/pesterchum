package pesterchum.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ServerSocketFactory;

import pesterchum.server.data.Manager;
import pesterchum.server.data.Interface;

public class Server {
	private boolean run;
	private ServerSocket server;
	private List<Connection> clients;
	private Manager database;
	public Server(int port){
		//setup database connection
		database = new Manager();
		Interface i = new Interface(database);
		database.registerInterface("message", i);
		database.registerInterface("login", i);
		database.registerInterface("admin", i);
		database.registerInterface("friendrequest", i);
		//setup socket listener
		ServerSocketFactory sslserversocketfactory = createServerSocketFactory();
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
		System.out.println("Server started on port "+server.getLocalPort());
		while(run){
			Socket socket = null;
			try {
				socket = server.accept();
			}catch(IOException e){
				System.err.println("Could not accept connection");
			}
			if(socket!=null){
				Connection conn = new Connection(socket, database, this);
				clients.add(conn);
			}
		}
	}
	public void disconnect(Connection conn){
		if(clients.contains(conn)){
			conn.close();
			clients.remove(conn);
		}
	}
	private ServerSocketFactory createServerSocketFactory() {
		return ServerSocketFactory.getDefault();
	}

}
