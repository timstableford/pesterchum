package pesterchum.client;

import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

import argo.format.CompactJsonFormatter;
import argo.format.JsonFormatter;
import argo.jdom.JsonRootNode;

public class Util {
	private static final JsonFormatter JSON_FORMATTER = new CompactJsonFormatter();
	/**
	 * Converts a string and font and a rectangle2d bounds
	 */
	public static Rectangle2D stringSize(String s, Font f){
		Rectangle2D r = f.getStringBounds(s, 
				new FontRenderContext(null, 
						RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT, 
						RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT));
		return r;
	}
	/**
	 * Merges 2 byte arrays into 1
	 */
	public static byte[] mergeByteArrays(byte[] a, byte[] b){
		byte[] r = new byte[a.length+b.length];
		System.arraycopy(a, 0, r, 0, a.length);
		System.arraycopy(b, 0, r, a.length, b.length);
		return r;
	}
	/**
	 * Converts argo json to a string
	 */
	public static String jsonToString(JsonRootNode node){
		return JSON_FORMATTER.format(node);
	}
	/**
	 * Gets the upper-case initials of a pesterchum name
	 */
	public static String initial(String username){
		String initials = username.substring(0, 1).toUpperCase();
		for(int i=1; i<username.length(); i++){
			char s = username.charAt(i);
			if(s>='A'&&s<='Z'){
				initials = initials + s;
				break;
			}
		}
		return initials;
	}
	/**
	 * Checks if a username conforms to pesterchum standards
	 */
	public static boolean verifyUsername(String username){
		//check exactly 1 capital
		int capitalcount = 0;
		for(int i=0; i<username.length(); i++){
			char c = username.charAt(i);
			if(c>='A'&&c<='Z'){
				if(i==0){
					return false;
				}
				capitalcount++;
			}
		}
		if(capitalcount!=1){
			return false;
		}
		//check only letters
		String ut = username.toString().toLowerCase();
		for(int j=0; j<ut.length(); j++){
			char c = ut.charAt(j);
			if(c>'z'||c<'a'){
				return false;
			}
		}
		return true;
	}
	/**
	 * Gets the reason for failure of a username to pesterchum, if there is one
	 * @param username username to check
	 * @return null or reason
	 */
	public static String usernameFailureReason(String username){
		//check exactly 1 capital
		int capitalcount = 0;
		for(int i=0; i<username.length(); i++){
			char c = username.charAt(i);
			if(c>='A'&&c<='Z'){
				if(i==0){
					return "First letter must not be upper case";
				}
				capitalcount++;
			}
		}
		if(capitalcount!=1){
			return "Must contain exactly 1 upper case letter, not first";
		}
		//check only letters
		String ut = username.toString().toLowerCase();
		for(int j=0; j<ut.length(); j++){
			char c = ut.charAt(j);
			if(c>'z'||c<'a'){
				return "Must be only letters";
			}
		}
		return null;
	}
}
