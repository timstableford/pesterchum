package pesterchum.client.data;

import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonObjectNodeBuilder;
import argo.jdom.JsonRootNode;

import uk.co.tstableford.utilities.Utilities;

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
		this.from = new String((Utilities.decodeHex(data.getData().getStringValue("from"))));
		this.to = new String((Utilities.decodeHex(data.getData().getStringValue("to"))));
		this.content = new String((Utilities.decodeHex(data.getData().getStringValue("content"))));
		this.time = Long.parseLong(data.getData().getStringValue("time"));
	}
	public JsonRootNode getJson(){
		JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder()
				.withField("class", JsonNodeBuilders.aStringBuilder("message"))
				.withField("from", JsonNodeBuilders.aStringBuilder(Utilities.encodeHex(from.getBytes())))
				.withField("to", JsonNodeBuilders.aStringBuilder(Utilities.encodeHex(to.getBytes())))
				.withField("content", JsonNodeBuilders.aStringBuilder(Utilities.encodeHex(content.getBytes())))
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
