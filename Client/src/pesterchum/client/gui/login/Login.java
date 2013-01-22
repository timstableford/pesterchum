package pesterchum.client.gui.login;

import java.awt.GridLayout;

import pesterchum.client.data.Interface;
import pesterchum.client.gui.GUI;
import pesterchum.client.gui.theme.PButton;
import pesterchum.client.gui.theme.PFrame;
import pesterchum.client.gui.theme.PLabel;
import pesterchum.client.gui.theme.PPasswordField;
import pesterchum.client.gui.theme.PTextField;

public class Login extends PFrame{
	private PTextField un;
	private PPasswordField pw;
	public Login(GUI gui, Interface ifa){
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
		this.add(unl);
		this.add(un);
		this.add(pnl);
		this.add(pw);
		this.add(login);
		this.pack();
		this.setVisible(true);
	}
}
