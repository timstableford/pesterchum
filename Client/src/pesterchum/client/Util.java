package pesterchum.client;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import argo.format.CompactJsonFormatter;
import argo.format.JsonFormatter;
import argo.jdom.JsonRootNode;

public class Util {
	private static final JsonFormatter JSON_FORMATTER = new CompactJsonFormatter();
	public static byte[] mergeByteArrays(byte[] a, byte[] b){
		byte[] r = new byte[a.length+b.length];
		System.arraycopy(a, 0, r, 0, a.length);
		System.arraycopy(b, 0, r, a.length, b.length);
		return r;
	}
	public static String jsonToString(JsonRootNode node){
		return JSON_FORMATTER.format(node);
	}
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
	public static String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		Node nValue = nlList.item(0);
		return nValue.getNodeValue();
	}
	public static Element getFirst(Document doc, String name){
		NodeList nList = doc.getElementsByTagName(name);
		Node nNode = nList.item(0);
		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			return (Element) nNode;
		}
		return null;
	}
}
