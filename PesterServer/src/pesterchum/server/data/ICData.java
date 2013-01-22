package pesterchum.server.data;

import pesterchum.server.Connection;

public class ICData {
	private String name, data;
	private Connection source;
	public ICData(String name, String data, Connection source){
		this.name = name;
		this.data = data;
		this.source = source;
	}
	public String getName(){
		return name;
	}
	public String getData(){
		return data;
	}
	public Connection getSource(){
		return source;
	}
}
