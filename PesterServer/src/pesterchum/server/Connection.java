package pesterchum.server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLSocket;

public class Connection implements Runnable{
	private BufferedReader in;
	private OutputStream out;
	private boolean run;
	private Socket socket;
	public Connection(Socket socket){
		this.socket = socket;
		run = true;
		(new Thread(this)).start();
	}
	public void write(String data){
		byte[] o = (data+"\n").getBytes();
		try {
			out.write(o);
		} catch (IOException e) {
			System.err.println("Couldn't write to "+socket.getInetAddress());
		}
	}
	private void processIncoming(String data){
		System.out.println(data);
	}
	@Override
	public void run() {
		System.out.println("Connection from "+socket.getInetAddress());	
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = socket.getOutputStream();
		} catch (IOException e) {
			run = false;
		}
		while(run){
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
