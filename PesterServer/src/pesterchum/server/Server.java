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
import pesterchum.server.data.database.Database;
import uk.co.tstableford.utilities.Log;

public class Server {
	private boolean run;
	private ServerSocket server;
	private List<Connection> clients;
	private Manager manager;
	public Server(int port, Database database){
		//setup database connection
		manager = new Manager(database);
		Interface i = new Interface(manager);
		manager.registerInterface("message", i);
		manager.registerInterface("login", i);
		manager.registerInterface("admin", i);
		manager.registerInterface("friendrequest", i);
		//setup socket listener
		ServerSocketFactory sslserversocketfactory = createServerSocketFactory();
		try {
			server = sslserversocketfactory.createServerSocket(port);
		} catch (IOException e) {
			Log.getInstance().error("Could not bind to socket at "+port);
			System.exit(-1);
		}
		clients = Collections.synchronizedList(new ArrayList<Connection>());
		run = true;
		run();
	}
	public void run(){
		Log.getInstance().info("Server started on port "+server.getLocalPort());
		while(run){
			Socket socket = null;
			try {
				socket = server.accept();
			}catch(IOException e){
				Log.getInstance().error("Could not accept connection");
			}
			if(socket!=null){
				Connection conn = new Connection(socket, manager, this);
				clients.add(conn);
			}
		}
	}
	public void disconnect(Connection conn){
		if(conn!=null&&clients.contains(conn)){
			conn.close();
			clients.remove(conn);
		}
	}
	private ServerSocketFactory createServerSocketFactory() {
		return ServerSocketFactory.getDefault();
	}

}
