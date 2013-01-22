package pesterchum.client.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import pesterchum.client.Util;
import pesterchum.client.connection.Encryption;

public class Message {
	private String to, from, message;
	private long time;
	private DocumentBuilder builder;
	public Message(String from, String to, String message){
		this.to = to;
		this.from = from;
		this.message = message;
		this.time = System.currentTimeMillis();
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			builder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println("Couldn't setup document builder");
		}
	}
	public Message(ICData data){
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			builder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println("Couldn't setup document builder");
		}
		try {
			Document doc = builder.parse(new ByteArrayInputStream(data.getData().getBytes()));
			Element e = Util.getFirst(doc, "message");
			this.from = new String(Encryption.decode(Util.getTagValue("from", e)));
			this.to = new String(Encryption.decode(Util.getTagValue("to", e)));
			this.message = new String(Encryption.decode(Util.getTagValue("content", e)));
			this.time = Long.parseLong(Util.getTagValue("time", e));
		} catch (SAXException | IOException e1) {
			System.err.println("Could not convert message from XML");
		}
	}
	public String getXML(){
		Document doc = builder.newDocument();
		Element root = doc.createElement("message");
		doc.appendChild(root);

		Element from = doc.createElement("from");
		from.appendChild(doc.createTextNode(Encryption.encode(this.from.getBytes())));
		root.appendChild(from);

		Element to = doc.createElement("to");
		to.appendChild(doc.createTextNode(Encryption.encode(this.to.getBytes())));
		root.appendChild(to);

		Element message = doc.createElement("content");
		message.appendChild(doc.createTextNode(Encryption.encode(this.message.getBytes())));
		root.appendChild(message);

		Element time = doc.createElement("time");
		time.appendChild(doc.createTextNode(this.time+""));
		root.appendChild(time);

		return Util.docToString(doc);
	}
	public String getMessage(){
		return this.message;
	}
	public String getFrom(){
		return this.from;
	}
	public String getTo(){
		return this.to;
	}
	public long getTime(){
		return time;
	}
}
