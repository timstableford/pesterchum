package pesterchum.server;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class KeyGen {
	private SecretKey key;
	public KeyGen(){
		KeyGenerator kg;
		try {
			kg = KeyGenerator.getInstance("AES");
			kg.init(128);
			key = kg.generateKey();
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Could not generate encryption key");
		}	
	}
	public SecretKey getKey(){
		return key;
	}
}
