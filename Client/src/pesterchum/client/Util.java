package pesterchum.client;

import java.io.CharArrayWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Util {
	public static byte[] mergeByteArrays(byte[] a, byte[] b){
		byte[] r = new byte[a.length+b.length];
		System.arraycopy(a, 0, r, 0, a.length);
		System.arraycopy(b, 0, r, a.length, b.length);
		return r;
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
	public static String docToString(Document doc){
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			CharArrayWriter writer = new CharArrayWriter();
			StreamResult result = new StreamResult(writer);
			transformer.transform(source, result);
			return writer.toString();
		} catch (TransformerException e) {
			return null;
		}
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
