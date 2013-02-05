package pesterchum.client.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import pesterchum.client.connection.SettingsException;
import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

public class Language {
	private JdomParser parser;
	private HashMap<String, String> lang;
	public Language() throws IOException{
		load(this.getClass().getResourceAsStream("/pesterchum/client/config/english.json"));
	}
	public Language(String internalPath) throws IOException{
		load(this.getClass().getResourceAsStream(internalPath));
	}
	public Language(File file) throws IOException{
		if(!file.exists()){
			throw new FileNotFoundException();
		}else{
			load(new FileInputStream(file));
		}
	}
	public void load(InputStream is) throws IOException{
		parser = new JdomParser();
		lang = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		JsonRootNode rn = null;
		try {
			rn = parser.parse(br);
		} catch (InvalidSyntaxException e) {
			throw new SettingsException("Error parsing json");
		}
		List<JsonNode> arr = rn.getArrayNode("settings");
		for(JsonNode n: arr){
			lang.put(n.getStringValue("from"), n.getStringValue("to"));
		}
	}
	public String get(String key){
		key = key.toLowerCase();
		if(!lang.containsKey(key)){
			return key;
		}
		return lang.get(key);
	}
}
