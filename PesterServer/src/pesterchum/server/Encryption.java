package pesterchum.server;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {
	private SecretKeySpec sks;
	private Cipher enc, denc;
	public Encryption(byte[] key){
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
	public Encryption(){
		this(generateKey());
	}
	public byte[] getKey(){
		return sks.getEncoded();
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
	public String encrypt(String data){
		try {
			data = new String(Encryption.encode(enc.doFinal(data.getBytes())));
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			System.err.println("Could not encrypt data");
		}
		return data;
	}
	public String decrypt(String data){
		try {
			data = new String(denc.doFinal(Encryption.decode(data)));
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			System.err.println("Decoding and decryption failed");
		}
		return data;
	}
	public static String encode(byte[] data){
		StringBuffer out = new StringBuffer();
		for(int i=0; i<data.length; i++){
			int a = data[i];
			a = a + 128;
			out.append("%");
			out.append(a);
		}
		return out.toString();
	}
	public static byte[] decode(String data){
		if(data.contains("%")){
			String[] a = data.split("%");
			byte[] ret = new byte[a.length-1];
			for(int i=1; i<a.length; i++){
				int j = Integer.parseInt(a[i]);
				j = j - 128;
				ret[i-1] = (byte)j;
			}
			return ret;
		}
		return null;
	}
}
