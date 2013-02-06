package uk.co.tstableford.secureconnection.common;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import uk.co.tstableford.utilities.Utilities;


public class PubKeyEncryption {
	private RSAPublicKeySpec pub;
	private PublicKey pubKey;
	private RSAPrivateKeySpec priv;
	private EncryptionMode mode;
	//DECRYPTION, decryptee, key creator, public key sender
	public PubKeyEncryption(){
		this.mode = EncryptionMode.DECRYPT;
		initAsymmetric();
	}
	private void initAsymmetric(){
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048);
			KeyPair kp = kpg.genKeyPair();
			KeyFactory fact = KeyFactory.getInstance("RSA");
			pub = fact.getKeySpec(kp.getPublic(), RSAPublicKeySpec.class);
			priv = fact.getKeySpec(kp.getPrivate(), RSAPrivateKeySpec.class);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			System.err.println("Could not create public private encryption");
		}
	}
	public String getPublicKeyString(){
		if(mode==EncryptionMode.DECRYPT){
			StringBuffer b = new StringBuffer();
			b.append("type:publickey;modulus:");
			b.append(Utilities.encodeHex(pub.getModulus().toString().getBytes()));
			b.append(";exponent:");
			b.append(Utilities.encodeHex(pub.getPublicExponent().toString().getBytes()));
			b.append(";");
			return b.toString();
		}else{
			return null;
		}
	}
	public byte[] decrypt(String data){
		try {
			KeyFactory fact = KeyFactory.getInstance("RSA");
			PrivateKey privKey = fact.generatePrivate(priv);
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privKey);
			byte[] cipherData = cipher.doFinal(Utilities.decodeHex(data));
			return cipherData;
		} catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException 
				| NoSuchPaddingException | InvalidKeyException | InvalidKeySpecException e) {
			return null;
		}
	}
	
	//ENCRYPTION, public key receiver, encryptee
	public PubKeyEncryption(BigInteger modulus, BigInteger exponent) throws NoSuchAlgorithmException, InvalidKeySpecException{
		this.mode = EncryptionMode.ENCRYPT;
		initAsymmetric(modulus, exponent);
	}
	public void initAsymmetric(BigInteger modulus, BigInteger exponent) throws NoSuchAlgorithmException, InvalidKeySpecException{
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exponent);
		KeyFactory fact = KeyFactory.getInstance("RSA");
		pubKey = fact.generatePublic(keySpec);
	}
	public String encrypt(byte[] data){
		if(mode==EncryptionMode.ENCRYPT){
			try {
				Cipher cipher = Cipher.getInstance("RSA");
				cipher.init(Cipher.ENCRYPT_MODE, pubKey);
				byte[] cipherData = cipher.doFinal(data);
				return Utilities.encodeHex(cipherData);
			} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException 
					| NoSuchAlgorithmException | NoSuchPaddingException e) {
				System.err.println("Could not encrypt data");
				return null;
			}
		}else{
			return null;
		}
	}
	
	public enum EncryptionMode{
		ENCRYPT,
		DECRYPT;
	}
}
