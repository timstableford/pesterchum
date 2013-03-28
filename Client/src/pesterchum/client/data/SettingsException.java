package pesterchum.client.data;

import java.io.IOException;

public class SettingsException extends IOException{
	private static final long serialVersionUID = 1L;

	public SettingsException(String text){
		super(text);
	}
}
