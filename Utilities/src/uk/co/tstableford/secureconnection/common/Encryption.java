package uk.co.tstableford.secureconnection.common;
/**
 * Wraps encryption methods
 * @author Tim Stableford
 * Wraps symmetric and asymmetric encryption
 */
public class Encryption {
	private SymmetricEncryption symmetric;
	private PubKeyEncryption pubKey;
	public Encryption(){
		
	}
	public boolean isSymmetricInit(){
		return this.symmetric!=null;
	}
	public void setSymmetric(SymmetricEncryption s){
		this.symmetric = s;
	}
	public boolean isPubKey(){
		return this.pubKey!=null;
	}
	public void setPubKey(PubKeyEncryption e){
		this.pubKey = e;
	}
	public SymmetricEncryption getSymmetric(){
		return this.symmetric;
	}
	public PubKeyEncryption getPubKey(){
		return this.pubKey;
	}
}
