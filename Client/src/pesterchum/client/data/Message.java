package pesterchum.client.data;

public class Message {
	private String to, from, message;
	private long time;
	public Message(String from, String to, String message){
		this.to = to;
		this.from = from;
		this.message = message;
		this.time = System.currentTimeMillis();
	}
	public Message(ICData incoming){
		//TODO convert xml to message
	}
	public String getXML(){
		//TODO implement this shit
		return null;
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
