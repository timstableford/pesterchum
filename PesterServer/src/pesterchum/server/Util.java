package pesterchum.server;

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
