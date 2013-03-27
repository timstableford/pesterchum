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
import uk.co.tstableford.utilities.Log;

public class Connection implements Incoming{
	private SecureConnection conn;
	private String username;
	private Hashtable<String, IncomingJson> interfaces;
	private final JdomParser parser = new JdomParser();
	private Log log;
	private Interface ifa;
	public Connection(Interface ifa, Log log){
		this.username = null;
		this.log = log;
		this.ifa = ifa;
		this.interfaces = new Hashtable<String, IncomingJson>();
	}
	public boolean connect(String host, int port){
		log.info("Connecting to "+host+" on port "+port);
		Socket s;
		try {
			s = new Socket(host, port);
		} catch (IOException e) {
			return false;
		}
		conn = new SecureClientConnection(s, this, this.log);
		return true;
	}
	public void close(){
		if(conn!=null){
			conn.close();
			conn = null;
		}
		this.username = null;
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
			log.error("Could not parse incoming data");
		}
	}
	private IncomingJson getIncoming(String name){
		return interfaces.get(name);
	}
	@Override
	public void timeout() {
		this.close();
		ifa.timeout();
	}
}
