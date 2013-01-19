package pesterchum.client.resource;

import java.io.InputStream;

public interface Resource {
	public boolean load(InputStream is);
	public String getName();
}
