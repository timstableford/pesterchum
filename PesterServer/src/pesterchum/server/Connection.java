package pesterchum.server;

import java.net.Socket;

import argo.jdom.JdomParser;
import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonObjectNodeBuilder;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

import pesterchum.server.data.Manager;
import pesterchum.server.data.ICData;
import pesterchum.server.data.User;
import uk.co.tstableford.secureconnection.common.interfaces.Incoming;
import uk.co.tstableford.secureconnection.common.interfaces.SecureConnection;
import uk.co.tstableford.secureconnection.server.SecureServerConnection;
import uk.co.tstableford.utilities.Log;

public class Connection implements Incoming{
	private SecureConnection conn;
	private User user;
	private Manager database;	
	private Server server;
	private final JdomParser parser = new JdomParser();
	public Connection(Socket socket, Manager database, Server server){
		this.database = database;
		this.server = server;
		Log.getInstance().info("Connection from "+socket.getInetAddress());
		this.conn = new SecureServerConnection(socket, this);
	}
	public User getUser(){
		return this.user;
	}
	public void setUser(User user){
		this.user = user;
	}
	public void ready(){
		JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder()
				.withField("class", JsonNodeBuilders.aStringBuilder("admin"))
				.withField("command", JsonNodeBuilders.aStringBuilder("hello"))
				.withField("version", JsonNodeBuilders.aStringBuilder(Integer.toString(Run.VERSION)));
		JsonRootNode n = builder.build();
		conn.write(Util.jsonToString(n));
	}
	public void close(){
		if(this.conn!=null&&this.user!=null){
			database.removeUser(this.user.getUsername());
			this.user = null;
			conn.close();
			conn = null;
			server.disconnect(this);
		}
	}
	public SecureConnection getConn(){
		return conn;
	}
	@Override
	public synchronized void processIncoming(byte[] arg0) {
		try {
			JsonRootNode node = parser.parse(new String(arg0));
			String name = node.getStringValue("class");
			database.getInterface(name).processIncoming(new ICData(name, node, this));
		} catch (InvalidSyntaxException e) {
			Log.getInstance().error("Error parsing incoming json from "+this.getConn().getSource());
			Log.getInstance().debug(new String(arg0), 3);
		}
	}
	@Override
	public void timeout() {
		Log.getInstance().error("Connection timeout from "+this.getConn().getSource());
		close();
	}

}
