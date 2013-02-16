package uk.co.tstableford.secureconnection.server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.SAXException;

import uk.co.tstableford.secureconnection.common.Encryption;
import uk.co.tstableford.secureconnection.common.IncomingRunner;
import uk.co.tstableford.secureconnection.common.interfaces.Incoming;
import uk.co.tstableford.secureconnection.common.interfaces.SecureConnection;
import uk.co.tstableford.utilities.Log;
/**
 * Server half of the secure connection
 * @author Tim Stableford
 *
 */
public class SecureServerConnection implements Runnable, SecureConnection{
	private static final String LOG_FILE = "secure_server.log";
	private static final long TIMEOUT = 15000;
	private long lastPong; //last pong receive
	private long lastPing; //last ping sent
	private BufferedReader in;
	private BufferedOutputStream out;
	private Log log;
	private boolean run;
	private Socket socket;
	private Encryption enc;
	private Incoming handler;
	private List<byte[]> writeBuffer;
	/**
	 * @param socket A connected socket to use
	 * @param handler the upstream class to pass data to
	 * Uses default log file
	 */
	public SecureServerConnection(Socket socket, Incoming handler){
		this(socket, handler, new Log(new File(LOG_FILE), 5, false));
	}
	/**
	 * @param socket A connected socket to use
	 * @param handler the upstream class to pass data to
	 * @param log the log to use
	 */
	public SecureServerConnection(Socket socket, Incoming handler, Log log){
		this.log = log;
		this.socket = socket;
		this.handler = handler;
		this.enc = new Encryption();
		log.info("[SC]Setting up");
		writeBuffer = Collections.synchronizedList(new LinkedList<byte[]>());
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new BufferedOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			log.error("[SC]Could not setup streams");
		}
		run = true;
		(new Thread(this)).start();
	}
	@Override
	/**
	 * @param data writes encrypted data to the connection
	 */
	public void write(String data) {
		log.debug("[SC]Adding to write buffer - "+data, 4);
		writeBuffer.add(data.getBytes());
	}
	@Override
	/**
	 * @return string representation of the connected address
	 */
	public String getSource() {
		return this.socket.getInetAddress().toString();
	}
	@Override
	/**
	 * @return the encryption wrapper class
	 */
	public Encryption getEncryption() {
		return this.enc;
	}
	@Override
	/**
	 * Used to change the handler after initialisation
	 * @param handler the upstream class to set to
	 */
	public void setHandler(Incoming handler) {
		this.handler = handler;
	}
	@Override
	/**
	 * @return true if fully encrypted
	 */
	public boolean encrypted() {
		return enc.isSymmetricInit();
	}
	@Override
	/**
	 * Gracefully stops threads and closes socket
	 */
	public void close() {
		log.debug("[SC]Close requested", 5);
		if(socket!=null){
			log.debug("[SC]Closing socket",4);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				//Silly impatient thread
			}
			try {
				out.close();
				in.close();
				socket.close();
			} catch (IOException e) {
				//we're going to close it anyway
			}
			socket = null;
			run = false;
		}
	}
	/**
	 * Writes data without buffering encrypted
	 * @param data data to write
	 * This will block until sent
	 */
	public synchronized void writeNow(String data){
		log.debug("[SC]Writing now unencrypted - "+data, 4);
		if(out!=null){
			try {
				if(!data.endsWith("\n")){
					data = data + "\n";
				}
				out.write(data.getBytes());
				out.flush();
			} catch (IOException e) {
				log.error("[SC]Could not write to socket");
			}	
		}else{
			log.error("[SC]Could not write to socket");
		}
	}
	private void processIncoming(String data) throws SAXException, IOException{
		log.debug("[SC]Incoming data - "+data, 4);
		new IncomingRunner(data, this.handler, this, log);
	}
	public Incoming getHandler(){
		return handler;
	}
	@Override
	public void run() {
		lastPong = System.currentTimeMillis();
		while(run){
			timeout();
			sendPing();
			try {
				if(in!=null&&in.ready()){
					processIncoming(in.readLine());
				}
			} catch (IOException | SAXException e) {
				log.error("Error reading from server");
				break; //going to assume lost connection and break loop
			}
			//we're only going to do this writing if we're secure
			if(out!=null&&writeBuffer.size()>0&&enc.isSymmetricInit()){
				try {
					//here we remove from the write buffer, encrypt it and send it
					byte[] ts = writeBuffer.remove(0);
					log.debug("[SC]Encrypting and writing "+new String(ts), 4);
					out.write((enc.getSymmetric().encrypt(ts)+"\n").getBytes());
					out.flush();
				} catch (IOException e) {
					//TODO implement log
				}
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				//It doesn't really matter if we ignore this
			}
		}
	}
	private void timeout(){
		if((System.currentTimeMillis()-lastPong)>TIMEOUT){
			log.debug("Timeout", 5);
			handler.timeout();
		}
	}
	private void sendPing(){
		if((System.currentTimeMillis()-lastPing)>(TIMEOUT/3)){
			log.debug("Sending ping", 5);
			this.writeNow("type:ping;");
			lastPing = System.currentTimeMillis();
		}
	}
	@Override
	/**
	 * Never called, server is never pinged only ponged
	 */
	public void ping() {
		//should never be pinged, just here for pretty interface
	}
	/**
	 * Registers a pong
	 */
	@Override
	public void pong() {
		log.debug("Ponged", 5);
		lastPong = System.currentTimeMillis();
	}
}
