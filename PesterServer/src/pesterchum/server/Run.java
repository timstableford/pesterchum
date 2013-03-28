package pesterchum.server;

import java.io.File;
import java.sql.SQLException;

import pesterchum.server.data.database.Database;
import pesterchum.server.data.database.SQLiteDatabase;

import uk.co.tstableford.utilities.Log;

public class Run {
	public static int VERSION = 3;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean verbose = false;
		int debug = 0;
		File l = new File("server_log.log");
		File d = new File("database.db");
		for(int i=0; i<args.length; i++){
			switch(args[i]){
			case "-verbose":
				verbose = true;
				break;
			case "-debug":
				if(i+1<args.length){
					try{
						Integer.parseInt(args[i+1]);
					}catch(NumberFormatException e){
						System.out.println("debug level not an number");
						System.exit(0);
					}
					i++;
				}else{
					System.out.println("no debug level specified");
					System.exit(0);
				}
				break;
			case "-log":
				if(i+1<args.length){
					l = new File(args[i+1]);
					i++;
				}else{
					System.out.println("no log file specified");
					System.exit(0);
				}
				break;
			case "-db":
				if(i+1<args.length){
					d = new File(args[i+1]);
					i++;
				}else{
					System.out.println("no database file specified");
					System.exit(0);
				}
				break;
			default:
				System.err.println("unknown option - "+args[i]);
				System.exit(0);
				break;
			}
		}
		Log.setInstance(new Log(l, debug, verbose));
		Database database = null;
		try {
			database = new SQLiteDatabase(d.getPath());
		} catch (ClassNotFoundException | SQLException e) {
			System.err.println("Could not open database");
			System.exit(0);
		}
		new Server(7423, database);
	}

}
