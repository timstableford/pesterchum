package uk.co.tstableford.secureconnection.common.interfaces;

import uk.co.tstableford.secureconnection.common.Encryption;
/**
 * Designates common methods for the server and client secure connections
 * @author Tim Stableford
 *
 */
public interface SecureConnection {
	public void write(String data);
	public void writeNow(String data);
	public String getSource();
	public Encryption getEncryption();
	public void setHandler(Incoming handler);
	public Incoming getHandler();
	public boolean encrypted();
	public void close();
	public void ping();
	public void pong();
}
