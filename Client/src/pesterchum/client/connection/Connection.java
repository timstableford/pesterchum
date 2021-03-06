package pesterchum.client.connection;

import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;

import argo.jdom.JdomParser;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

import pesterchum.client.data.ICData;
import uk.co.tstableford.secureconnection.client.SecureClientConnection;
import uk.co.tstableford.secureconnection.common.interfaces.Incoming;
import uk.co.tstableford.secureconnection.common.interfaces.SecureConnection;
import uk.co.tstableford.utilities.Log;

public class Connection implements Incoming{
	private SecureConnection conn;
	private Hashtable<String, IncomingJson> interfaces;
	private final JdomParser parser = new JdomParser();
	private Interface ifa;
	public Connection(Interface ifa){
		this.ifa = ifa;
		this.interfaces = new Hashtable<String, IncomingJson>();
	}
	public boolean connect(String host, int port){
		Log.getInstance().info("Connecting to "+host+" on port "+port);
		Socket s;
		try {
			s = new Socket(host, port);
		} catch (IOException e) {
			return false;
		}
		conn = new SecureClientConnection(s, this, Log.getInstance());
		return true;
	}
	public void close(){
		if(conn!=null){
			conn.close();
			conn = null;
		}
	}
	public void ready(){
		//we dont need to do anything on connection open
	}
	public void registerIncoming(String name, IncomingJson inc){
		interfaces.put(name, inc);
	}
	public SecureConnection getConnection(){
		return conn;
	}
	@Override
	public void processIncoming(byte[] arg0) {
		try {
			JsonRootNode rn = parser.parse(new String(arg0));
			getIncoming(rn.getStringValue("class")).processIncoming(new ICData(rn.getStringValue("class"), rn));
		} catch (InvalidSyntaxException e) {
			Log.getInstance().error(e.getMessage());
			Log.getInstance().error("Could not parse incoming data");
		}
	}
	private IncomingJson getIncoming(String name){
		return interfaces.get(name);
	}
	@Override
	public void timeout() {
		ifa.timeout();
		Log.getInstance().info("Timeout");
		this.close();
	}
}
