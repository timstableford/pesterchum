package pesterchum.client.data;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonObjectNodeBuilder;
import argo.jdom.JsonRootNode;

import uk.co.tstableford.utilities.Utilities;

public class Message {
	private String to, from, content;
	private long time;
	private boolean offline;
	public Message(String from, String to, String content){
		this.to = to;
		this.from = from;
		this.content = content;
		this.time = System.currentTimeMillis();
		offline = true;
	}
	public Message(ICData data) throws IOException{
		if(!data.getData().getStringValue("class").equals("message")){
			throw new IOException("Data not message");
		}
		this.from = new String((Utilities.decodeHex(data.getData().getStringValue("from"))));
		this.to = new String((Utilities.decodeHex(data.getData().getStringValue("to"))));
		this.content = new String((Utilities.decodeHex(data.getData().getStringValue("content"))));
		this.time = Long.parseLong(data.getData().getStringValue("time"));
		this.offline = Boolean.parseBoolean(data.getData().getStringValue("offline"));
		if(!this.getHash().equals(data.getData().getStringValue("hash"))){
			throw new IOException("Hash does not match data");
		}
	}
	public JsonRootNode getJson(){
		JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder()
				.withField("class", JsonNodeBuilders.aStringBuilder("message"))
				.withField("hash", JsonNodeBuilders.aStringBuilder(getHash()))
				.withField("from", JsonNodeBuilders.aStringBuilder(Utilities.encodeHex(from.getBytes())))
				.withField("to", JsonNodeBuilders.aStringBuilder(Utilities.encodeHex(to.getBytes())))
				.withField("content", JsonNodeBuilders.aStringBuilder(Utilities.encodeHex(content.getBytes())))
				.withField("time", JsonNodeBuilders.aStringBuilder(Long.toString(time)))
				.withField("offline", JsonNodeBuilders.aStringBuilder(Boolean.toString(offline)));
		return builder.build();
	}
	public String getHash(){
        try {
        	MessageDigest cript = MessageDigest.getInstance("SHA-1");
            cript.reset();
			cript.update((from+to+content+time).getBytes("utf8"));
			return Utilities.encodeHex(cript.digest());
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			System.err.println("Could not get hash");
		}
        return null;
	}
	public void allowOffline(boolean allow){
		this.offline = allow;
	}
	public boolean allowsOffline(){
		return offline;
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
	public void setTime(Long time){
		this.time = time;
	}
	public long getTime(){
		return time;
	}
}
