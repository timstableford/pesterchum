package uk.co.tstableford.utilities;

import java.util.HashMap;
/**
 * Misc functions class
 * @author Tim Stableford
 *
 */
public class Utilities {
	/**
	 * Decodes key/value data to hashmap
	 * @param data the string representation
	 * @return the decoded hashmap
	 * Format is [key]:[value]; where ; denotes end of line and multiple lines can be used
	 */
	public static HashMap<String, String> decode(String data){
		HashMap<String, String> in = null;
		if(data.contains(";")){
			in = new HashMap<String, String>();
			while(data.contains(";")){
				String[] split = data.split(";", 2);
				data = split[1];
				if(split[0].contains(":")){
					String[] split2 = split[0].split(":", 2);
					in.put(split2[0], split2[1]);
				}
			}
		}
		return in;
	}
	/**
	 * Encodes a byte array to ascii hex
	 * @param bytes byte array to encode to hex
	 * @return encoded byte array as hex
	 */
	public static String encodeHex(byte[] bytes) {
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
	/**
	 * Decodes ascii hex to a byte array
	 * @param hex string of ascii hex to decode
	 * @return the decoded byte array
	 */
	public static byte[] decodeHex(String hex){
		int len = hex.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
	                             + Character.digit(hex.charAt(i+1), 16));
	    }
	    return data;
	}
}
