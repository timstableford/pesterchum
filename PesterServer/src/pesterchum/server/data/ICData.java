package pesterchum.server.data;

import argo.jdom.JsonRootNode;
import pesterchum.server.Connection;

public class ICData {
	private String name;
	private JsonRootNode data;
	private Connection source;
	public ICData(String name, JsonRootNode data, Connection source){
		this.name = name;
		this.data = data;
		this.source = source;
	}
	public String getName(){
		return name;
	}
	public JsonRootNode getData(){
		return data;
	}
	public Connection getSource(){
		return source;
	}
}
