package pesterchum.client.connection;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import argo.format.JsonFormatter;
import argo.format.PrettyJsonFormatter;
import argo.jdom.JdomParser;
import argo.jdom.JsonArrayNodeBuilder;
import argo.jdom.JsonNode;
import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonObjectNodeBuilder;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;
import static argo.jdom.JsonNodeBuilders.*;

public class Settings{
	private File file;
	private final JsonFormatter formatter;
	private final JdomParser parser;
	private HashMap<String, String> settings, defaults;
	public Settings(File file) throws IOException{
		this.file = file;
		formatter = new PrettyJsonFormatter();
		parser = new JdomParser();
		load(file);
	}
	@SuppressWarnings("unchecked")
	public void load(File file) throws IOException{
		settings = new HashMap<String, String>();
		if(!file.exists()){
			(new File(file.getParent())).mkdirs();
			file.createNewFile();
			loadDefaults();
			settings = (HashMap<String, String>) defaults.clone();
			save();
		}
		BufferedReader is = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		JsonRootNode rn = null;
		try {
			rn = parser.parse(is);
		} catch (InvalidSyntaxException e) {
			throw new SettingsException("Error parsing json");
		}
		List<JsonNode> arr = rn.getArrayNode("settings");
		for(JsonNode n: arr){
			this.setString(n.getStringValue("key"), n.getStringValue("value"));
		}
	}
	public void save() throws IOException{
		JsonRootNode rn = getJSON();
		String out = formatter.format(rn);
		OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
		PrintWriter writer = new PrintWriter(os);
		writer.write(out);
		writer.flush();
		writer.close();
	}
	public JsonRootNode getJSON(){
		JsonArrayNodeBuilder sets = anArrayBuilder();
		for(Entry<String, String> e: settings.entrySet()){
			sets.withElement(JsonNodeBuilders.anObjectBuilder()
					.withField("key", aStringBuilder(e.getKey()))
					.withField("value", aStringBuilder(e.getValue())));
		}
		JsonObjectNodeBuilder builder = anObjectBuilder() 
				.withField("settings", sets); 
		JsonRootNode json = builder.build();
		return json;
	}
	public void setString(String key, String value){
		if(settings.containsKey(key)){
			settings.remove(key);
		}
		settings.put(key, value);
	}
	public void setInt(String key, int value){
		if(settings.containsKey(key)){
			settings.remove(key);
		}
		settings.put(key, Integer.toString(value));
	}
	public int getInt(String key) throws SettingsException{
		String s = getString(key);
		try{
			Integer.parseInt(s);
		}catch(NumberFormatException e){
			throw new SettingsException("Key "+key+" not an int");
		}
		return Integer.parseInt(s);
	}
	public String getString(String key) throws SettingsException{
		if(!settings.containsKey(key)){
			if(defaults.containsKey(key)){
				this.setString(key, defaults.get(key));
			}else{
				throw new SettingsException("Key "+key+" not found");
			}
		}
		return settings.get(key);
	}
	private void loadDefaults() throws SettingsException{
		defaults = new HashMap<String, String>();
		BufferedReader is = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/pesterchum/client/config/defaults.json")));
		JsonRootNode rn = null;
		try {
			rn = parser.parse(is);
		} catch (InvalidSyntaxException | IOException e) {
			throw new SettingsException("Error parsing json");
		}
		List<JsonNode> arr = rn.getArrayNode("settings");
		for(JsonNode n: arr){
			defaults.put(n.getStringValue("key"), n.getStringValue("value"));
		}
	}
}
