package pesterchum.server;

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
	public static void printArray(byte[] a){
		for(int i=0; i<a.length; i++){
			System.out.print((char)a[i]);
		}
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
		Node nValue = (Node) nlList.item(0);
		return nValue.getNodeValue();
	}
}
