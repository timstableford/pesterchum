package pesterchum.client.data;

import argo.jdom.JsonRootNode;

public class ICData {
	private String name;
	private JsonRootNode data;
	public ICData(String name, JsonRootNode data){
		this.name = name;
		this.data = data;
	}
	public String getName(){
		return name;
	}
	public JsonRootNode getData(){
		return data;
	}
}
