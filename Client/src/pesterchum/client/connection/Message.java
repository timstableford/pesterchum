package pesterchum.client.connection;

public class Message {
	private String to, from, message;
	private long time;
	public Message(String from, String to, String message){
		this.to = to;
		this.from = from;
		this.message = message;
		this.time = System.currentTimeMillis();
	}
	public String getXML(){
		//TODO implement this shit
		return null;
	}
}
