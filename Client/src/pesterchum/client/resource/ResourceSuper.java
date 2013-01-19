package pesterchum.client.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Loads resources
 * @author Tim Stableford
 *
 */
public abstract class ResourceSuper implements Resource{
	private String name;
	public ResourceSuper(String name, String path){
		this.name = name;
		InputStream in = getClass().getResourceAsStream(path); 
		this.load(in);
		try {
			in.close();
		} catch (IOException e) {
			//TODO deal with this
		}
	}
	public ResourceSuper(String path){
		this(new File(path).getName(), path);	
	}
	public String getName(){
		return name;
	}
}
