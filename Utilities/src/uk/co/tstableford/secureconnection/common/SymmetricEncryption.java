package uk.co.tstableford.secureconnection.common;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import uk.co.tstableford.utilities.Utilities;

public class SymmetricEncryption {
	private SecretKeySpec sks;
	private Cipher enc, denc;
	public SymmetricEncryption(byte[] key){
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
	public SymmetricEncryption(){
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
	public byte[] getSymmetricKey(){
		return sks.getEncoded();
	}
	public String encrypt(byte[] data){
		try {
			return new String(Utilities.encodeHex(enc.doFinal(data)));
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			System.err.println("Could not encrypt data");
		}
		return null;
	}
	public byte[] decrypt(String data){
		try {
			return denc.doFinal(Utilities.decodeHex(data));
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			System.err.println("Decoding and decryption failed");
		}
		return null;
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
}
