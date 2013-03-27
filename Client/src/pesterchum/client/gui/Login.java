package pesterchum.client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import pesterchum.client.Util;
import pesterchum.client.connection.Interface;
import pesterchum.client.connection.SettingsException;
import pesterchum.client.gui.theme.PButton;
import pesterchum.client.gui.theme.PFrame;
import pesterchum.client.gui.theme.POpaqueLabel;
import pesterchum.client.gui.theme.PMenuBar;
import pesterchum.client.gui.theme.PMenuItem;
import pesterchum.client.gui.theme.PPanel;
import pesterchum.client.gui.theme.PPasswordField;
import pesterchum.client.gui.theme.PTabbedPane;
import pesterchum.client.gui.theme.PTextField;

public class Login extends PFrame implements ActionListener, Runnable, KeyListener{
	private static final long serialVersionUID = 5329488003668890739L;
	private PTextField un, run, server, port;
	private PPasswordField pw, rpw, rpwr;
	private PButton login, register, save;
	private PMenuItem min, quit;
	private Interface ifa;
	private String u,p;
	private boolean clicked;
	private Action action;
	public Login(Interface ifa) throws SettingsException{
		super();
		this.ifa = ifa;
		this.clicked = false;
		this.setTitle("Pesterchum "+ifa.translate("login"));
		this.setLocation(200,200);
		this.setSize(new Dimension(200,240));
		this.setUndecorated(true);
		//menu
		PMenuBar mb = new PMenuBar();
		mb.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.gridx = 0;
		mb.add(Box.createHorizontalGlue(), c);
		c.weightx = 0;
		//icons
		c.gridx = 1;
		min = new PMenuItem("_");
		mb.add(min, c);
		min.addActionListener(this);
		c.gridx = 2;
		quit = new PMenuItem("X");
		mb.add(quit, c);
		quit.addActionListener(this);
		this.add(mb, BorderLayout.NORTH);

		PTabbedPane tabs = new PTabbedPane();

		tabs.addTab(ifa.translate("login").toUpperCase(), createLoginPanel());
		tabs.addTab(ifa.translate("register").toUpperCase(), createRegisterPanel());
		tabs.addTab(ifa.translate("server").toUpperCase(), createServerPanel());
		this.add(tabs, BorderLayout.CENTER);
		this.setMinimumSize(this.getSize());
		this.setVisible(true);
	}
	public PPanel createServerPanel() throws SettingsException{
		PPanel serverPanel = new PPanel();
		GridLayout layout = new GridLayout(0,1);
		layout.setHgap(3);
		serverPanel.setLayout(layout);
		server = new PTextField(16);
		port = new PTextField(16);
		server.setText(ifa.getSettings().getString("host"));
		port.setText(ifa.getSettings().getString("port"));
		serverPanel.add(server);
		serverPanel.add(port);
		save = new PButton("SAVE");
		save.addActionListener(this);
		serverPanel.add(save);
		return serverPanel;
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
		register = new PButton(ifa.translate("register"));
		register.addActionListener(this);
		registerPanel.add(new POpaqueLabel(ifa.translate("username")));
		registerPanel.add(run);
		registerPanel.add(new POpaqueLabel(ifa.translate("password")));
		registerPanel.add(rpw);
		registerPanel.add(new POpaqueLabel(ifa.translate("repeat")));
		registerPanel.add(rpwr);
		registerPanel.add(register);
		return registerPanel;
	}
	public PPanel createLoginPanel(){
		PPanel loginPanel = new PPanel();
		GridLayout layout = new GridLayout(0,1);
		layout.setHgap(3);
		loginPanel.setLayout(layout);
		POpaqueLabel unl, pnl;
		un = new PTextField(16);
		un.addKeyListener(this);
		pw = new PPasswordField(16);
		pw.addKeyListener(this);
		unl = new POpaqueLabel(ifa.translate("username"));
		pnl = new POpaqueLabel(ifa.translate("password"));
		login = new PButton(ifa.translate("login"));
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
		action = Action.LOGIN;
		if(!clicked&&ifa!=null&&u!=null&&p!=null){
			clicked = true;
			login.setEnabled(false);
			register.setEnabled(false);
			login.setText(ifa.translate("logging in")+"...");
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
				error(ifa.translate("passwords do not match"));
				return;
			}
			if(!Util.verifyUsername(u)){
				error(Util.usernameFailureReason(u));
				return;
			}
			this.u = u;
			this.p = pw;
			register.setText(ifa.translate("registering")+"...");
			login.setEnabled(false);
			register.setEnabled(false);
			clicked = true;
			(new Thread(this)).start();
		}
	}
	private void error(String error){
		JOptionPane.showMessageDialog(this,
				error,
				ifa.translate("error"),
				JOptionPane.ERROR_MESSAGE);
	}
	private void info(String info){
		JOptionPane.showMessageDialog(this, info,
				ifa.translate("information"), JOptionPane.INFORMATION_MESSAGE);
	}
	public void save(){
		String newServer = server.getText();
		int newPort = 0;
		try{
			newPort = Integer.parseInt(port.getText());
		}catch(NumberFormatException e){
			error(ifa.translate("port not a number"));
		}
		if(newPort>0){
			try{
				SocketAddress sockaddr = new InetSocketAddress(newServer, newPort);
				Socket socket = new Socket();
				// Connect with 10 s timeout
				socket.connect(sockaddr, 10000);
				if(!socket.isConnected()||socket.isClosed()){
					error(ifa.translate("could not contact server"));
				}else{
					ifa.getSettings().setInt("port", newPort);
					ifa.getSettings().setString("host", newServer);
					ifa.getSettings().save();
					info(ifa.translate("saved settings"));
				}
				socket.close();
			}catch(IOException e){
				error(ifa.translate("could not contact server"));
			}
		}
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getSource()==login){
			login();
		}else if(arg0.getSource()==register){
			register();
		}else if(arg0.getSource()==save){
			save();
		}else if(arg0.getSource()==min){
			this.setState(Frame.ICONIFIED);
		}else if(arg0.getSource()==quit){
			System.exit(0);
		}
	}
	@Override
	public void run() {
		try {
			if(ifa.connect(ifa.getSettings().getString("host"), ifa.getSettings().getInt("port"))){
				System.out.println("Connected to server");
			}else{
				System.err.println("Connection to server failed");
			}
		} catch (SettingsException e) {
			System.err.println("Error retrieving host/port configuration");
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
