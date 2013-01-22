package pesterchum.client.gui.login;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import pesterchum.client.connection.Interface;
import pesterchum.client.gui.theme.PButton;
import pesterchum.client.gui.theme.PFrame;
import pesterchum.client.gui.theme.PLabel;
import pesterchum.client.gui.theme.PPasswordField;
import pesterchum.client.gui.theme.PTextField;

public class Login extends PFrame implements ActionListener{
	private static final long serialVersionUID = 5329488003668890739L;
	private PTextField un;
	private PPasswordField pw;
	private Interface ifa;
	public Login(Interface ifa){
		this.ifa = ifa;
		this.setTitle("Pesterchum Login");
		GridLayout layout = new GridLayout(0,1);
		layout.setHgap(3);
		layout.setVgap(3);
		this.setLayout(layout);
		PLabel unl, pnl;
		PButton login;
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
		switch(arg0.getActionCommand().toUpperCase()){
		case "LOGIN":
			String u,p;
			u = un.getText();
			p = new String(pw.getPassword());
			if(u!=null&&p!=null){
				if(ifa.connect("localhost", 7423)){
					System.out.println("Connected to server");
				}else{
					System.err.println("Connection to server failed");
				}
				//TODO configurable host
				ifa.login(u,p);
			}
			break;
		default:
			System.err.println("Action command unknwon - "+arg0.getActionCommand().toUpperCase());
		}

	}
}
