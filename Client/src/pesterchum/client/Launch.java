package pesterchum.client;


import java.io.File;
import java.io.IOException;

import pesterchum.client.connection.Interface;
import pesterchum.client.data.Settings;
import pesterchum.client.gui.PesterchumGUI;
import pesterchum.client.gui.main.GUI;
import uk.co.tstableford.utilities.Log;

public class Launch {
	private static final String PC_DIR = ".pesterchum";
	private static final String LOG_FILE = "pesterchum.log";
	private static final String SETTINGS_FILE = "settings.json";
	private static final String F_S = System.getProperty("file.separator");
	private static final String U_H = System.getProperty("user.home");
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean verbose = false;
		int debug = 0;
		for(int i=0; i<args.length; i++){
			switch(args[i]){
			case "-verbose":
				verbose = true;
				break;
			case "-debug":
				try{
					debug = Integer.parseInt(args[i+1]);
					i++;
				}catch(NumberFormatException | ArrayIndexOutOfBoundsException e){
					System.err.println("Debug level unspecified or not a number, using default 0");
				}
				break;
			}
		}
		Settings settings = null;
		try {
			settings = new Settings(new File(U_H+F_S+PC_DIR+F_S+SETTINGS_FILE));
		} catch (IOException e1) {
			System.err.println("Error loading settings");
		}
		Log log = new Log(new File(U_H+F_S+PC_DIR+F_S+LOG_FILE), debug, verbose);
		
		//TODO implement ability to load gui from other packages
		PesterchumGUI gui = new GUI();
		try {
			Interface ifa = new Interface(gui, settings, log);
			gui.init(ifa);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error, could not load interface");
		}
		
	}

}
