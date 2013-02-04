package pesterchum.client.gui;

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;

import pesterchum.client.connection.Interface;
import pesterchum.client.gui.theme.PButton;
import pesterchum.client.gui.theme.PFrame;
import pesterchum.client.gui.theme.PLabel;
import pesterchum.client.gui.theme.PMenuBar;
import pesterchum.client.gui.theme.PMenuItem;
import pesterchum.client.gui.theme.PPasswordField;
import pesterchum.client.gui.theme.PTextField;
import pesterchum.client.resource.ResourceLoader;

public class Login extends PFrame implements ActionListener, Runnable{
	private static final long serialVersionUID = 5329488003668890739L;
	private PTextField un;
	private PPasswordField pw;
	private PButton login;
	private PMenuItem min, quit;
	private Interface ifa;
	private String host;
	private int port;
	private String u,p;
	private boolean clicked;
	public Login(String host, int port, Interface ifa){
		super();
		this.host = host;
		this.port = port;
		this.ifa = ifa;
		this.clicked = false;
		this.setTitle("Pesterchum Login");
		this.setLocation(200,200);
		GridLayout layout = new GridLayout(0,1);
		layout.setHgap(3);
		layout.setVgap(3);
		this.setLayout(layout);
		this.setUndecorated(true);
		//menu
		PMenuBar mb = new PMenuBar();
		mb.add(Box.createHorizontalStrut(140));
		min = new PMenuItem("_");
		mb.add(min);
		min.addActionListener(this);
		quit = new PMenuItem("X");
		mb.add(quit);
		quit.addActionListener(this);
		this.add(mb);
		//buttons and inpout
		PLabel unl, pnl;
		un = new PTextField(16);
		pw = new PPasswordField(16);
		unl = new PLabel("Username");
		pnl = new PLabel("Password");
		login = new PButton("Login");
		login.addActionListener(this);
		this.add(unl);
		this.add(un);
		this.add(pnl);
		this.add(pw);
		this.add(login);
		this.pack();
		this.setVisible(true);
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		login.setActionCommand("LOGIN");
		if(arg0.getSource()==login){
			u = un.getText();
			p = new String(pw.getPassword());
			if(!clicked&&ifa!=null&&u!=null&&p!=null){
				clicked = true;
				login.setEnabled(false);
				login.setText("Logging in...");
				(new Thread(this)).start();
			}
		}else if(arg0.getSource()==min){
			this.setState(Frame.ICONIFIED);
		}else if(arg0.getSource()==quit){
			System.exit(0);
		}
	}
	@Override
	public void run() {
		if(ifa.connect(host, port)){
			System.out.println("Connected to server");
		}else{
			System.err.println("Connection to server failed");
		}
		ifa.login(u,p);
		clicked = false;
	}
	public void loginResponse(boolean success){
		if(!success){
			login.setText("LOGIN - FAILED");
			login.setEnabled(true);
		}
	}
}
