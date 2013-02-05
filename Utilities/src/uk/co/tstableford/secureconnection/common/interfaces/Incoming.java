package uk.co.tstableford.secureconnection.common.interfaces;

public interface Incoming {
	public void processIncoming(byte[] data);
	public void timeout();
}
