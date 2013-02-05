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
	private boolean verbose = false;
	private BufferedWriter out = null;
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
	public int debugLevel(){
		return debug;
	}
	public synchronized void info(String data){
		write("[INFO]"+data);
		if(verbose){
			System.out.println("[INFO]"+data);
		}
	}
	public synchronized void debug(String data, int level){
		if(debug>=level){
			write("[DEBUG][LEVEL"+level+"]"+data);
			if(verbose){
				System.out.println("[DEBUG][LEVEL"+level+"]"+data);
			}
		}
	}
	public synchronized void error(String data){
		if(debug>0){
			write("[ERROR]"+data);
			if(verbose){
				System.err.println("[ERROR]"+data);
			}
		}
	}
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
