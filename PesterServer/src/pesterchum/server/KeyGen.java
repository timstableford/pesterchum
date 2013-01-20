package pesterchum.server;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class KeyGen {
	private PrivateKey pri;
	private PublicKey pub;
	public KeyGen(){
		KeyPairGenerator KPG;
		try {
			KPG = KeyPairGenerator.getInstance("RSA");
			KPG.initialize(1024);
			KeyPair kp = KPG.generateKeyPair();
			pri = kp.getPrivate();
			pub = kp.getPublic();
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Could not generate keys");
		}
	}
	public PrivateKey getPrivateKey(){
		return pri;
	}
	public PublicKey getPublicKey(){
		return pub;
	}
}
