package pesterchum.client.data;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import argo.jdom.JsonArrayNodeBuilder;
import argo.jdom.JsonNode;
import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonObjectNodeBuilder;
import argo.jdom.JsonRootNode;

public class User {
	private String username;
	private boolean authenticated;
	private List<String> friends;
	private Color color;
	public User(String username){
		this.username = username;
		this.authenticated = false;
		this.friends = Collections.synchronizedList(new LinkedList<String>());
		this.color = Color.black;
	}
	public User(JsonNode json){
		this.friends = Collections.synchronizedList(new LinkedList<String>());
		this.authenticated = true;
		List<JsonNode> arr = json.getArrayNode("friends");
		for(JsonNode n: arr){
			this.friends.add(n.getStringValue("username"));
		}
		this.username = json.getStringValue("username");
		JsonNode c = json.getNode("color");
		this.color = new Color(Integer.parseInt(c.getStringValue("red")),
				Integer.parseInt(c.getStringValue("green")),
				Integer.parseInt(c.getStringValue("blue")));
	}
	public void addFriend(String username){
		friends.add(username);
	}
	public Color getColor(){
		return color;
	}
	public void setColor(Color c){
		this.color = c;
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
	public JsonObjectNodeBuilder getJson(){
		JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder();
		builder.withField("username", JsonNodeBuilders.aStringBuilder(this.username));
		JsonArrayNodeBuilder friends = JsonNodeBuilders.anArrayBuilder();
		for(String f: this.friends){
			friends.withElement(
					JsonNodeBuilders.anObjectBuilder()
					.withField("username", JsonNodeBuilders.aStringBuilder(f)));
		}
		builder.withField("friends", friends);
		JsonObjectNodeBuilder c = JsonNodeBuilders.anObjectBuilder();
		c.withField("red", JsonNodeBuilders.aStringBuilder(Integer.toString(color.getRed())));
		c.withField("green", JsonNodeBuilders.aStringBuilder(Integer.toString(color.getGreen())));
		c.withField("blue", JsonNodeBuilders.aStringBuilder(Integer.toString(color.getBlue())));
		builder.withField("color", c);
		return builder;
	}
	public List<String> getFriends(){
		return friends;
	}
	public boolean hasFriend(String username){
		for(String s: friends){
			if(s.equalsIgnoreCase(username)){
				return true;
			}
		}
		return false;
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
