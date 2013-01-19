package pesterchum.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLSocket;

public class Connection implements Runnable{
	private BufferedReader in;
	private OutputStream out;
	private boolean run;
	private SSLSocket socket;
	private List<byte[]> writeBuffer;
	public Connection(SSLSocket socket){
		this.socket = socket;
		writeBuffer = Collections.synchronizedList(new LinkedList<byte[]>());
		run = true;
		System.out.println("Connection from "+socket.getInetAddress());	
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = socket.getOutputStream();
		} catch (IOException e) {
			run = false;
		}
		(new Thread(this)).start();
	}
	public void write(byte[] data){
		writeBuffer.add(data);
	}
	private void processIncoming(String data){
		System.out.println(data);
	}
	@Override
	public void run() {
		while(run){
			if(writeBuffer.size()>0){
				try {
					out.write(writeBuffer.get(0));
					writeBuffer.remove(0);
				} catch (IOException e) {
					System.err.println("Couldn't write to "+socket.getInetAddress());
				}
			}
			try {
				if(in.ready()){
					processIncoming(in.readLine());
				}
			} catch (IOException e1) {
				System.err.println("Could not read from "+socket.getInetAddress());
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				//Thread can't sleep, silly insomniac
			}
		}
	}

}
