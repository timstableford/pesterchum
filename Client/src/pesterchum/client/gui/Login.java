package pesterchum.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import pesterchum.client.connection.Interface;
import pesterchum.client.gui.theme.PButton;
import pesterchum.client.gui.theme.PFrame;
import pesterchum.client.gui.theme.PLabel;
import pesterchum.client.gui.theme.PMenuBar;
import pesterchum.client.gui.theme.PMenuItem;
import pesterchum.client.gui.theme.PPanel;
import pesterchum.client.gui.theme.PPasswordField;
import pesterchum.client.gui.theme.PTabbedPane;
import pesterchum.client.gui.theme.PTextField;

public class Login extends PFrame implements ActionListener, Runnable, KeyListener{
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
		this.add(mb, BorderLayout.NORTH);

		PTabbedPane tabs = new PTabbedPane();
		
		tabs.addTab("LOGIN", createLoginPanel());
		tabs.addTab("REGISTER", createRegisterPanel());
		this.add(tabs, BorderLayout.CENTER);

		this.pack();
		this.setVisible(true);
	}
	public PPanel createRegisterPanel(){
		PPanel registerPanel = new PPanel();
		registerPanel.add(new JLabel("register here"));
		return registerPanel;
	}
	public PPanel createLoginPanel(){
		PPanel loginPanel = new PPanel();
		GridLayout layout = new GridLayout(0,1);
		layout.setHgap(3);
		loginPanel.setLayout(layout);
		PLabel unl, pnl;
		un = new PTextField(16);
		un.addKeyListener(this);
		pw = new PPasswordField(16);
		pw.addKeyListener(this);
		unl = new PLabel("Username");
		pnl = new PLabel("Password");
		login = new PButton("Login");
		login.addActionListener(this);
		loginPanel.add(unl);
		loginPanel.add(un);
		loginPanel.add(pnl);
		loginPanel.add(pw);
		loginPanel.add(login);
		return loginPanel;
	}
	public void login(){
		u = un.getText();
		p = new String(pw.getPassword());
		if(!clicked&&ifa!=null&&u!=null&&p!=null){
			clicked = true;
			login.setEnabled(false);
			login.setText("Logging in...");
			(new Thread(this)).start();
		}
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		login.setActionCommand("LOGIN");
		if(arg0.getSource()==login){
			login();
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
	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_ENTER){
			login();
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {}
}
