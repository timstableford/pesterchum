package pesterchum.client.data;

import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonObjectNodeBuilder;
import argo.jdom.JsonRootNode;

import pesterchum.client.connection.Encryption;

public class Message {
	private String to, from, content;
	private long time;
	public Message(String from, String to, String content){
		this.to = to;
		this.from = from;
		this.content = content;
		this.time = System.currentTimeMillis();
	}
	public Message(ICData data){
		this.from = new String(Encryption.decode(data.getData().getStringValue("from")));
		this.to = new String(Encryption.decode(data.getData().getStringValue("to")));
		this.content = new String(Encryption.decode(data.getData().getStringValue("content")));
		this.time = Long.parseLong(data.getData().getStringValue("time"));
	}
	public JsonRootNode getJson(){
		JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder()
				.withField("class", JsonNodeBuilders.aStringBuilder("message"))
				.withField("from", JsonNodeBuilders.aStringBuilder(from))
				.withField("to", JsonNodeBuilders.aStringBuilder(to))
				.withField("content", JsonNodeBuilders.aStringBuilder(content))
				.withField("time", JsonNodeBuilders.aStringBuilder(Long.toString(time)));
		return builder.build();
	}
	public String getContent(){
		return this.content;
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
