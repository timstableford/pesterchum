package pesterchum.server;

public class ICData {
	private String name, data;
	public ICData(String name, String data){
		this.name = name;
		this.data = data;
	}
	public String getName(){
		return name;
	}
	public String getData(){
		return data;
	}
}
