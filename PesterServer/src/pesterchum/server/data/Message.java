package pesterchum.server.data;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonObjectNodeBuilder;

import uk.co.tstableford.utilities.Log;
import uk.co.tstableford.utilities.Utilities;

public class Message extends Packet{
	private static final String type = "message";
	private String content;
	public Message(String from, String to, String content){
		super(from, to);
		this.content = content;
	}
	public Message(ICData data) throws IOException{
		super(data);
		if(!data.getData().getStringValue("type").equals(type)){
			throw new IOException("Data not message");
		}
		this.content = new String((Utilities.decodeHex(data.getData().getStringValue("content"))));
		if(!this.getHash().equals(data.getData().getStringValue("hash"))){
			throw new IOException("Hash does not match data");
		}
	}
	@Override
	public JsonObjectNodeBuilder getJson(){
		JsonObjectNodeBuilder builder = super.getJson()
				.withField("type", JsonNodeBuilders.aStringBuilder(type))
				.withField("hash", JsonNodeBuilders.aStringBuilder(getHash()))
				.withField("content", JsonNodeBuilders.aStringBuilder(Utilities.encodeHex(content.getBytes())));
		return builder;
	}
	public String getHash(){
        try {
        	MessageDigest cript = MessageDigest.getInstance("SHA-1");
            cript.reset();
			cript.update((super.getHash()+this.content).getBytes("utf8"));
			return Utilities.encodeHex(cript.digest());
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			Log.getInstance().error("Could not convert message to hash");
		}
        return null;
	}
	public String getContent(){
		return this.content;
	}
	@Override
	public String getType() {
		return type;
	}
}
