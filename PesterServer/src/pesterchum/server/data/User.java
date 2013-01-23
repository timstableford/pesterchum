package pesterchum.server.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pesterchum.server.Encryption;
import pesterchum.server.Util;

public class User {
	private String username;
	private boolean authenticated;
	private List<String> friends;
	public User(String username){
		this.username = username;
		this.authenticated = false;
		this.friends = Collections.synchronizedList(new LinkedList<String>());
	}
	public void addFriend(String username){
		friends.add(username);
	}
	public String getFriendsXML(){
		try {
			DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = b.newDocument();
			Element root = doc.createElement("friends");
			doc.appendChild(root);
			for(int i=0; i<friends.size(); i++){
				Element friend = doc.createElement("friend");
				root.appendChild(friend);
				Element name = doc.createElement("name");
				name.appendChild(doc.createTextNode(Encryption.encode(friends.get(i).getBytes())));
				friend.appendChild(name);
			}
			
			return Util.docToString(doc);
		} catch (ParserConfigurationException e) {
			return null;
		}
	}
	public void loadFriends(String xml){
		try {
			DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = b.parse(new ByteArrayInputStream(xml.getBytes()));
			NodeList nList = doc.getElementsByTagName("friend");
			for(int i=0; i<nList.getLength(); i++){
				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element e = (Element) nNode;
					addFriend(new String(Encryption.decode(Util.getTagValue("name", e))));
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException e1) {
			System.err.println("Could not load friends for "+username);
			e1.printStackTrace();
		}
	}
	@Override
	public String toString(){
		return username+" - "+friends.toString();
	}
	/*
	public boolean authenticate(boolean success){
		//TODO implement authentication
		/*
		 * 1)Hash Password
		 * 2)Check against database
		 * 4)Set authenticated
		 * 3)Dispose of password
		 *
		this.authenticated = true;
		return true;
	}*/
	public void setAuthenticated(boolean authenticated){
		this.authenticated = authenticated;
	}
	public boolean authenticated(){
		return this.authenticated;
	}
	public String getUsername(){
		return this.username;
	}
}
