package pesterchum.client.connection;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import pesterchum.client.Util;

public class Encryption {
	private SecretKeySpec sks;
	private Cipher enc, denc;
	private RSAPublicKeySpec pub;
	private RSAPrivateKeySpec priv;
	public Encryption(){
		initAsymmetric();
	}
	public void initAsymmetric(){
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
	public void initSymmetric(byte[] key){
		this.sks = new SecretKeySpec(key, "AES");
		try {
			enc = Cipher.getInstance("AES");
			enc.init(Cipher.ENCRYPT_MODE, sks);
		    denc = Cipher.getInstance("AES");
		    denc.init(Cipher.DECRYPT_MODE, sks);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			System.err.println("Error setting of encoders");
		}
	}
	public Secure secure(){
		if(pub==null&&enc==null){
			return Secure.NO;
		}else if(pub!=null&&enc==null){
			return Secure.PUBLICKEY;
		}else if(pub!=null&&enc!=null){
			return Secure.YES;
		}
		return Secure.NO;
	}
	public void reset(){
		sks = null;
		enc = null;
		denc = null;
		pub = null;
		priv = null;
		initAsymmetric();
	}
	public String getPublicKeyXML(){
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.newDocument();
			Element root = doc.createElement("publickey");
			doc.appendChild(root);
			
			Element modulus = doc.createElement("modulus");
			modulus.appendChild(doc.createTextNode(encode(pub.getModulus().toByteArray())));
			root.appendChild(modulus);
			
			Element exp = doc.createElement("exponent");
			exp.appendChild(doc.createTextNode(encode(pub.getPublicExponent().toByteArray())));
			root.appendChild(exp);
			
			return Util.docToString(doc);
		} catch (ParserConfigurationException e) {
			return null;
		}
	}
	public byte[] decryptAsymmetric(String data){
		try {
			KeyFactory fact = KeyFactory.getInstance("RSA");
			PrivateKey privKey = fact.generatePrivate(priv);
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privKey);
			byte[] cipherData = cipher.doFinal(Encryption.decode(data));
			return cipherData;
		} catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException 
				| NoSuchPaddingException | InvalidKeyException | InvalidKeySpecException e) {
			return null;
		}
	}
	public byte[] getKey(){
		return sks.getEncoded();
	}
	public String encryptSymmetric(byte[] data){
		try {
			return new String(Encryption.encode(enc.doFinal(data)));
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			System.err.println("Could not encrypt data");
		}
		return null;
	}
	public byte[] decryptSymmetric(String data){
		try {
			return denc.doFinal(Encryption.decode(data));
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			System.err.println("Decoding and decryption failed");
		}
		return null;
	}
	public static String encode(byte[] bytes) {
	    final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	public static byte[] decode(String hex){
		int len = hex.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
	                             + Character.digit(hex.charAt(i+1), 16));
	    }
	    return data;
	}
}