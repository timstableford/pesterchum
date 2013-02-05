package uk.co.tstableford.secureconnection.common.interfaces;

import uk.co.tstableford.secureconnection.common.Encryption;

public interface SecureConnection {
	public void write(String data);
	public void writeNow(String data);
	public String getSource();
	public Encryption getEncryption();
	public void setHandler(Incoming handler);
	public boolean encrypted();
	public void close();
	public void ping();
	public void pong();
}
