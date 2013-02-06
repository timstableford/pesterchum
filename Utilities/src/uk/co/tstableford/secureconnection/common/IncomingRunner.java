package uk.co.tstableford.secureconnection.common;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;

import uk.co.tstableford.secureconnection.common.interfaces.Incoming;
import uk.co.tstableford.secureconnection.common.interfaces.SecureConnection;
import uk.co.tstableford.utilities.Log;
import uk.co.tstableford.utilities.Utilities;


public class IncomingRunner implements Runnable{
	private String data;
	private Incoming handler;
	private SecureConnection conn;
	private Log log;
	public IncomingRunner(String data, Incoming handler, SecureConnection conn, Log log){
		this.log = log;
		this.data = data;
		this.handler = handler;
		this.conn = conn;
		(new Thread(this)).start();
	}
	@Override
	public void run() {
		if(!isSpecialCase(this.data)){
			if(conn.getEncryption().isSymmetricInit()){
				log.debug("Decrypting and passing to handler - "+this.data, 4);
				byte[] data = conn.getEncryption().getSymmetric().decrypt(this.data);
				if(handler!=null){
					handler.processIncoming(data);
				}
			}
		}
	}
	private boolean isSpecialCase(String data){
		HashMap<String, String> in = Utilities.decode(data);
		if(in!=null){
			switch(in.get("type")){
			case "symmetrickey":
				log.debug("Received symmetric key", 4);
				String key = in.get("key");
				byte[] decrypted = conn.getEncryption().getPubKey().decrypt(key);
				conn.getEncryption().setSymmetric(new SymmetricEncryption(decrypted));
				return true;
			case "publickey":
				log.debug("Received public key", 4);
				BigInteger mod = new BigInteger(new String(Utilities.decodeHex(in.get("modulus"))));
				BigInteger exp = new BigInteger(new String(Utilities.decodeHex(in.get("exponent"))));
				try {
					conn.getEncryption().setPubKey(new PubKeyEncryption(mod, exp));
					conn.getEncryption().setSymmetric(new SymmetricEncryption());
					String encryptedKey = conn.getEncryption().getPubKey().encrypt(conn.getEncryption().getSymmetric().getSymmetricKey());
					String toSend = "type:symmetrickey;key:"+encryptedKey+";";
					conn.writeNow(toSend);
				} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
					//TODO implement logger
				}
				return true;
			case "ping":
				conn.ping();
				return true;
			case "pong":
				conn.pong();
				return true;
			default:
				return false;
			}
		}
		return false;
	}

}
