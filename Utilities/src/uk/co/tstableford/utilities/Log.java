package uk.co.tstableford.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
/**
 * A simple logging software
 * @author Tim Stableford
 *
 */
public class Log {
	private int debug = -1;
	private static Log instance;
	private boolean verbose = false;
	private BufferedWriter out = null;
	/**
	 * @return the statically stored instance of log
	 */
	public static Log getInstance(){
		return instance;
	}
	/**
	 * sets a statically stored instance of log
	 * @param inst the instance to store
	 */
	public static void setInstance(Log inst){
		instance = inst;
	}
	/**
	 * @param output The file to write to
	 * @param debug the debug level 0 is off and lowest
	 * @param verbose whether to output to console
	 */
	public Log(File output, int debug, boolean verbose){
		try{
			this.debug = debug;
			this.verbose = verbose;
			out = new BufferedWriter(new FileWriter(output, true));
			if(!output.canWrite()){
				System.err.println("[ERROR] Log permissions do not allow write");
			}
		}catch(IOException e){
			System.err.println("[ERROR] Could not create log");
		}
	}
	/**
	 * @return the current debug level
	 */
	public int debugLevel(){
		return debug;
	}
	/**
	 * @param data writes info type to log
	 */
	public synchronized void info(String data){
		write("[INFO]"+data);
		if(verbose){
			System.out.println("[INFO]"+data);
		}
	}
	/**
	 * writes debug data to log if debug>=level
	 * @param data data to write
	 * @param level the debug level in
	 */
	public synchronized void debug(String data, int level){
		if(debug>=level){
			write("[DEBUG][LEVEL"+level+"]"+data);
			if(verbose){
				System.out.println("[DEBUG][LEVEL"+level+"]"+data);
			}
		}
	}
	/**
	 * @param data write to log if debug>0
	 */
	public synchronized void error(String data){
		if(debug>0){
			write("[ERROR]"+data);
			if(verbose){
				System.err.println("[ERROR]"+data);
			}
		}
	}
	/**
	 * closes the log file
	 */
	public void close(){
		try {
			out.close();
		} catch (IOException e) {
			System.err.println("[ERROR] Log could not close file");
		}
	}
	private synchronized void write(String data){
		try{
			out.write(data);
			out.newLine();
			out.flush();
		} catch (IOException e) {
			System.err.println("[ERROR] Log could not write to file");
		}
	}
}
