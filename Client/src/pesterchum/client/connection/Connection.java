package pesterchum.client.connection;

import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;

import argo.jdom.JdomParser;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

import pesterchum.client.data.ICData;
import pesterchum.client.data.IncomingJson;
import uk.co.tstableford.secureconnection.client.SecureClientConnection;
import uk.co.tstableford.secureconnection.common.interfaces.Incoming;
import uk.co.tstableford.secureconnection.common.interfaces.SecureConnection;

public class Connection implements Incoming{
	private SecureConnection conn;
	private String username;
	private Hashtable<String, IncomingJson> interfaces;
	private final JdomParser parser = new JdomParser();
	public Connection(){
		this.username = null;
		this.interfaces = new Hashtable<String, IncomingJson>();
	}
	public boolean connect(String host, int port){
		System.out.println("Connecting to "+host+" on port "+port);
		try {
			Socket s = new Socket(host, port);
			conn = new SecureClientConnection(s, this);
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	public void close(){
		if(conn!=null){
			conn.close();
		}
		this.username = null;
	}
	public void registerIncoming(String name, IncomingJson inc){
		interfaces.put(name, inc);
	}
	public SecureConnection getConnection(){
		return conn;
	}
	public void setUsername(String username){
		this.username = username;
	}
	public String getUsername(){
		return this.username;
	}
	@Override
	public void processIncoming(byte[] arg0) {
		try {
			JsonRootNode rn = parser.parse(new String(arg0));
			getIncoming(rn.getStringValue("class")).processIncoming(new ICData(rn.getStringValue("class"), rn));
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
			System.err.println("Could not parse incoming data");
		}
	}
	private IncomingJson getIncoming(String name){
		return interfaces.get(name);
	}
}
