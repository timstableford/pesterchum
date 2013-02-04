package pesterchum.client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import pesterchum.client.Util;
import pesterchum.client.connection.Interface;
import pesterchum.client.connection.SettingsException;
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
	private PTextField un, run;
	private PPasswordField pw, rpw, rpwr;
	private PButton login, register;
	private PMenuItem min, quit;
	private Interface ifa;
	private String host;
	private int port;
	private String u,p;
	private boolean clicked;
	private Action action;
	public Login(Interface ifa) throws SettingsException{
		super();
		this.host = ifa.getSettings().getString("host");
		this.port = ifa.getSettings().getInt("port");
		this.ifa = ifa;
		this.clicked = false;
		this.setTitle("Pesterchum Login");
		this.setLocation(200,200);
		this.setSize(new Dimension(200,240));
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

		this.setVisible(true);
	}
	public PPanel createRegisterPanel(){
		PPanel registerPanel = new PPanel();
		GridLayout layout = new GridLayout(0,1);
		layout.setHgap(3);
		registerPanel.setLayout(layout);
		run = new PTextField(16);
		rpw = new PPasswordField(16);
		rpwr = new PPasswordField(16);
		run.addKeyListener(this);
		rpw.addKeyListener(this);
		rpwr.addKeyListener(this);
		register = new PButton("Register");
		register.addActionListener(this);
		registerPanel.add(new PLabel("Username"));
		registerPanel.add(run);
		registerPanel.add(new PLabel("Password"));
		registerPanel.add(rpw);
		registerPanel.add(new PLabel("Password Again"));
		registerPanel.add(rpwr);
		registerPanel.add(register);
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
		action = Action.REGISTER;
		if(!clicked&&ifa!=null&&u!=null&&p!=null){
			clicked = true;
			login.setEnabled(false);
			register.setEnabled(false);
			login.setText("Logging in...");
			(new Thread(this)).start();
		}
	}
	public void register(){
		String u = run.getText();
		String pw = new String(rpw.getPassword());
		String pwr = new String(rpwr.getPassword());
		action = Action.REGISTER;
		if(!clicked&&ifa!=null&&u!=null&&pw!=null&&pwr!=null){
			if(pw.equals(pwr)==false){
				registrationError("Passwords do not match");
			}
			if(!Util.verifyUsername(u)){
				registrationError(Util.usernameFailureReason(u));
			}
			this.u = u;
			this.p = pw;
			register.setText("Registering...");
			login.setEnabled(false);
			register.setEnabled(false);
			clicked = true;
			(new Thread(this)).start();
		}
	}
	private void registrationError(String error){
		JOptionPane.showMessageDialog(this,
			    error,
			    "Registration error",
			    JOptionPane.ERROR_MESSAGE);
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getSource()==login){
			login();
		}else if(arg0.getSource()==register){
			register();
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
		if(action==Action.LOGIN){
			ifa.login(u,p);
		}else if(action==Action.REGISTER){
			ifa.register(u, p);
		}
		
	}
	public void loginResponse(boolean success){
		if(!success){
			login.setText("LOGIN - FAILED");
			login.setEnabled(true);
			register.setEnabled(true);
			clicked = false;
		}
	}
	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_ENTER){
			JComponent s = (JComponent)e.getSource();
			if(s==un||s==pw){
				login();
			}else if(s==run||s==rpw||s==rpwr){
				register();
			}
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {}
	enum Action{
		REGISTER,
		LOGIN;
	}
}
