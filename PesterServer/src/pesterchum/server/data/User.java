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

import argo.jdom.JsonArrayNodeBuilder;
import argo.jdom.JsonNode;
import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonObjectNodeBuilder;
import argo.jdom.JsonRootNode;

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
	public JsonObjectNodeBuilder getFriendsJson(){
		JsonArrayNodeBuilder friends = JsonNodeBuilders.anArrayBuilder();
		for(String f: this.friends){
			friends.withElement(
					JsonNodeBuilders.anObjectBuilder()
					.withField("username", JsonNodeBuilders.aStringBuilder(f)));
		}
		JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder()
				.withField("friends", friends);
		return builder;
	}
	public List<String> getFriends(){
		return friends;
	}
	public void loadFriends(JsonRootNode json){
		List<JsonNode> arr = json.getArrayNode("friends");
		for(JsonNode n: arr){
			this.friends.add(n.getStringValue("username"));
		}
	}
	@Override
	public String toString(){
		return username+" - "+friends.toString();
	}
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
