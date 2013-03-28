package pesterchum.server.data;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonObjectNodeBuilder;

import uk.co.tstableford.utilities.Log;
import uk.co.tstableford.utilities.Utilities;

public abstract class Packet {
	private String to, from;
	private long time;
	private boolean offline;
	public Packet(String from, String to){
		this.to = to;
		this.from = from;
		this.time = System.currentTimeMillis();
		offline = true;
	}
	public Packet(ICData data) throws IOException{
		if(!data.getData().getStringValue("class").equals("packet")){
			throw new IOException("Data not packet");
		}
		this.from = new String((Utilities.decodeHex(data.getData().getStringValue("from"))));
		this.to = new String((Utilities.decodeHex(data.getData().getStringValue("to"))));
		this.time = Long.parseLong(data.getData().getStringValue("time"));
		this.offline = Boolean.parseBoolean(data.getData().getStringValue("offline"));
	}
	public JsonObjectNodeBuilder getJson(){
		JsonObjectNodeBuilder builder = JsonNodeBuilders.anObjectBuilder()
				.withField("class", JsonNodeBuilders.aStringBuilder("packet"))
				.withField("hash", JsonNodeBuilders.aStringBuilder(getHash()))
				.withField("from", JsonNodeBuilders.aStringBuilder(Utilities.encodeHex(from.getBytes())))
				.withField("to", JsonNodeBuilders.aStringBuilder(Utilities.encodeHex(to.getBytes())))
				.withField("time", JsonNodeBuilders.aStringBuilder(Long.toString(time)))
				.withField("offline", JsonNodeBuilders.aStringBuilder(Boolean.toString(offline)));
		return builder;
	}
	public String getHash(){
        try {
        	MessageDigest cript = MessageDigest.getInstance("SHA-1");
            cript.reset();
			cript.update((from+to+time).getBytes("utf8"));
			return Utilities.encodeHex(cript.digest());
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			Log.getInstance().error("Could not convert message to hash");
		}
        return null;
	}
	public abstract String getType();
	public void allowOffline(boolean allow){
		this.offline = allow;
	}
	public boolean allowsOffline(){
		return offline;
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
