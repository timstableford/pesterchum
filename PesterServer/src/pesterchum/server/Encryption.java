package pesterchum.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class Encryption {
	private SecretKeySpec sks;
	private Cipher enc, denc;
	private PublicKey pubKey;
	public Encryption(){}
	public void initAsymmetric(String pubKeyXML) throws ParserConfigurationException, SAXException, IOException, NoSuchAlgorithmException, InvalidKeySpecException{
		DocumentBuilder builder =  DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(new ByteArrayInputStream(pubKeyXML.getBytes()));
		Element e = Util.getFirst(doc, "publickey");
		BigInteger m = new BigInteger(Encryption.decode(Util.getTagValue("modulus", e)));
		BigInteger ex = new BigInteger(Encryption.decode(Util.getTagValue("exponent", e)));
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, ex);
		KeyFactory fact = KeyFactory.getInstance("RSA");
		pubKey = fact.generatePublic(keySpec);
	}
	public String encryptAsymmetric(byte[] data){
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			byte[] cipherData = cipher.doFinal(data);
			return Encryption.encode(cipherData);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException 
				| NoSuchAlgorithmException | NoSuchPaddingException e) {
			System.err.println("Could not encrypt data");
			return null;
		}
	}
	public byte[] getKey(){
		return sks.getEncoded();
	}
	public boolean secure(){
		return enc!=null&&denc!=null;
	}
	public void initSymmetric(){
		this.sks = new SecretKeySpec(generateKey(), "AES");
		try {
			enc = Cipher.getInstance("AES");
			enc.init(Cipher.ENCRYPT_MODE, sks);
		    denc = Cipher.getInstance("AES");
		    denc.init(Cipher.DECRYPT_MODE, sks);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			System.err.println("Error setting of encoders");
		}
	}
	private static byte[] generateKey(){
		KeyGenerator kg;
		try {
			kg = KeyGenerator.getInstance("AES");
			kg.init(128);
			return kg.generateKey().getEncoded();
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Could not generate encryption key");
		}	
		return null;
	}
	public String encryptSymmetric(String data){
		try {
			data = new String(Encryption.encode(enc.doFinal(data.getBytes())));
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			System.err.println("Could not encrypt data");
		}
		return data;
	}
	public String decryptSymmetric(String data){
		try {
			data = new String(denc.doFinal(Encryption.decode(data)));
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			System.err.println("Decoding and decryption failed");
		}
		return data;
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
