package uk.co.tstableford.secureconnection.common.interfaces;
/**
 * Used to pass data upstream
 * @author Tim Stableford
 * Meant to be implemented by the upstream class to receive data
 */
public interface Incoming {
	public void processIncoming(byte[] data);
	public void timeout();
}
