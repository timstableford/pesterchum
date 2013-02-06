package uk.co.tstableford.secureconnection.client;

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
import uk.co.tstableford.secureconnection.common.PubKeyEncryption;
import uk.co.tstableford.secureconnection.common.interfaces.Incoming;
import uk.co.tstableford.secureconnection.common.interfaces.SecureConnection;
import uk.co.tstableford.utilities.Log;

public class SecureClientConnection implements Runnable, SecureConnection{
	private static final String LOG_FILE = "secure_client.log";
	private static final long TIMEOUT = 15000;
	private BufferedReader in;
	private long lastPing;
	private BufferedOutputStream out;
	private Socket socket;
	private boolean run;
	private List<byte[]> writeBuffer;
	private Incoming handler;
	private Encryption enc;
	private Log log;
	public SecureClientConnection(Socket socket, Incoming handler, Log log){
		this.handler = handler;
		this.log = log;
		writeBuffer = Collections.synchronizedList(new LinkedList<byte[]>());
		log.info("[SC] Setting up");
		try {
			this.socket = socket;
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new BufferedOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			log.error("[SC] Could not open streams");
		}
		enc = new Encryption();
		run = true;
		(new Thread(this)).start();
	}
	public SecureClientConnection(Socket socket, Incoming handler){
		this(socket, handler, new Log(new File(LOG_FILE), 5, false));
	}
	public Encryption getEncryption(){
		return this.enc;
	}
	@Override
	public void setHandler(Incoming handler) {
		this.handler = handler;
	}
	public void close(){
		log.debug("[SC] Close called", 5);
		if(socket!=null){
			log.debug("[SC] Closing socket", 4);
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
	public synchronized void write(String data){
		log.debug("[SC] Writing "+data, 4);
		writeBuffer.add(data.getBytes());
	}
	public synchronized void writeNow(String data){
		log.debug("[SC] Writing now unencrypted - "+data, 4);
		if(out!=null){
			try {
				if(!data.endsWith("\n")){
					data = data + "\n";
				}
				out.write(data.getBytes());
				out.flush();
			} catch (IOException e) {
				log.error("[SC] Could not write to socket");
			}	
		}else{
			log.error("[SC] Could not write to socket");
		}
	}
	public boolean encrypted(){
		return enc.isSymmetricInit();
	}
	private void processIncoming(String data) throws SAXException, IOException{
		log.debug("[SC] Incoming data - "+data, 4);
		new IncomingRunner(data, handler, this, log);
	}
	public String getSource(){
		return this.socket.getInetAddress().toString();
	}
	@Override
	public void run(){
		//send the hello
		enc.setPubKey(new PubKeyEncryption());
		this.writeNow(enc.getPubKey().getPublicKeyString());
		//initialise the loop
		lastPing = System.currentTimeMillis();
		while(run){
			timeout(); //check if we've timed out
			try {
				if(in!=null&&in.ready()){
					processIncoming(in.readLine());
				}
			} catch (IOException | SAXException e) {
				log.error("[SC] Error reading from socket");
				break; //going to assume lost connection and break loop
			}
			//we're only going to do this writing if we're secure
			if(out!=null&&writeBuffer.size()>0&&enc.isSymmetricInit()){
				try {
					//here we remove from the write buffer, encrypt it and send it
					byte[] ts = writeBuffer.remove(0);
					log.debug("[SC]Encrypting and writing - "+new String(ts), 4);
					out.write((enc.getSymmetric().encrypt(ts)+"\n").getBytes());
					out.flush();
				} catch (IOException e) {
					log.error("[SC] Could not write to socket");
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
		if((System.currentTimeMillis()-lastPing)>TIMEOUT){
			handler.timeout();
		}
	}
	@Override
	public void ping() {
		//respond to ping with pong
		lastPing = System.currentTimeMillis();
		this.writeNow("type:pong;");
	}
	@Override
	public void pong() {
		//shouldnt't ever be ponged, just here because pretty interface
	}
}
