package pesterchum.client.gui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import pesterchum.client.Util;
import pesterchum.client.connection.Interface;
import pesterchum.client.data.Message;
import pesterchum.client.data.SettingsException;
import pesterchum.client.gui.PesterchumGUI;
import pesterchum.client.gui.main.theme.PButton;
import pesterchum.client.gui.main.theme.PFrame;
import pesterchum.client.gui.main.theme.PMenu;
import pesterchum.client.gui.main.theme.PMenuBar;
import pesterchum.client.gui.main.theme.PMenuItem;
import pesterchum.client.gui.main.theme.POpaqueLabel;
import pesterchum.client.gui.main.theme.PPanel;
import pesterchum.client.resource.ResourceLoader;
import uk.co.tstableford.utilities.Log;

public class GUI extends PFrame implements ActionListener, PesterchumGUI{
	private static final long serialVersionUID = 1L;
	private ResourceLoader smilies, theme;
	private PButton addchum, pester;
	private FriendPane friends;
	private Login login;
	private List<Messaging> messWindows;
	private Interface ifa; //this has the methods you will communicate with
	public GUI(){
		this.messWindows = new ArrayList<Messaging>();
		//this is called if anything needs to be done before the interface is setup
	}
	@Override
	public void init(Interface ifa) {
		this.ifa = ifa;
		this.setTitle("Pesterchum");
		this.setUndecorated(true);
		this.setLocation(200,200);
		try {
			login = new Login(ifa);
		} catch (SettingsException e) {
			Log.getInstance().error("Could not load settings");
			System.exit(-1);
		}

		// menu at top
		// logo
		//list of chums
		//buttons add chum/block[red]/pester!
		//mychumhandle: mood/ nick/color
		//mood table 2x6 + abscond

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		smilies = new ResourceLoader();
		smilies.load("/smilies/smilies.xml");
		theme = new ResourceLoader();
		theme.load("/theme/images.xml");
		this.setSize(new Dimension(230, 380));
		//create menu bar + menus

		PPanel content = new PPanel();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.PAGE_START;
		content.setLayout(new GridBagLayout());
		this.add(getMenu(), BorderLayout.NORTH);
		this.add(content, BorderLayout.CENTER);
		//logo
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 0;
		c.insets = new Insets(0,6,0,6);
		content.add(new JLabel(new ImageIcon(theme.getImage("logo")), SwingConstants.LEFT), c);
		//////
		c.gridy = 1;
		c.weighty = 1;
		content.add(getBuddyList(),c);
		//////
		c.weighty = 0;
		c.anchor = GridBagConstraints.PAGE_END;
		c.gridy = 2;
		content.add(Box.createVerticalStrut(5));
		c.gridy = 3;
		c.insets = new Insets(0,6,6,6);
		content.add(getMoods(),c);
		this.setMinimumSize(this.getSize());
	}
	private PMenuBar getMenu(){
		PMenuBar menu = new PMenuBar();
		menu.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		PMenu client = new PMenu(ifa.translate("client"));
		PMenu profile = new PMenu(ifa.translate("profile")); 
		PMenu help = new PMenu(ifa.translate("help")); 

		PMenuItem quit = new PMenuItem("X");
		PMenuItem min = new PMenuItem("_");
		quit.addActionListener(this);
		min.addActionListener(this);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		menu.add(Box.createHorizontalStrut(5), c);
		//menu
		c.gridx = 1;
		menu.add(client, c); 
		c.gridx = 2;
		menu.add(profile, c); 
		c.gridx = 3;
		menu.add(help, c);
		//spacer
		c.gridx = 4;
		c.weightx = 1;
		menu.add(Box.createHorizontalGlue(), c);
		c.weightx = 0;
		//exit buttons
		c.gridx = 5;
		menu.add(min, c);
		c.gridx = 6;
		menu.add(quit, c);
		//set up the Client menu
		PMenuItem options, memos, pesterLog, randomEncounter, userList, idle, addGroup;
		PMenuItem reconnect, exit;
		options = new PMenuItem(ifa.translate("options")); memos = new PMenuItem(ifa.translate("memos"));
		pesterLog = new PMenuItem(ifa.translate("pesterlog")); randomEncounter = new PMenuItem(ifa.translate("random encounter")); 
		userList = new PMenuItem(ifa.translate("user list")); idle = new PMenuItem(ifa.translate("idle")); 
		addGroup = new PMenuItem(ifa.translate("add group")); 
		reconnect = new PMenuItem(ifa.translate("reconnect")); exit = new PMenuItem(ifa.translate("exit"));
		exit.setActionCommand("X");
		exit.addActionListener(this);
		reconnect.addActionListener(this);
		reconnect.setActionCommand("reconnect");
		//TODO implmenent these
		//client.add(options); client.add(memos); client.add(pesterLog); client.add(randomEncounter);
		//client.add(userList); client.add(idle); client.add(addGroup);
		client.add(reconnect); client.add(exit);

		//set up the Profile menu
		PMenuItem quirks, trollSlum, color, switchChum;
		quirks = new PMenuItem(ifa.translate("quirks")); trollSlum = new PMenuItem(ifa.translate("trollslum")); 
		color = new PMenuItem(ifa.translate("color")); 
		color.addActionListener(this);
		color.setActionCommand("color");
		switchChum = new PMenuItem(ifa.translate("switch"));
		profile.add(quirks); profile.add(trollSlum); profile.add(color); profile.add(switchChum);

		//set up the Help menu
		PMenuItem helpMe, about, reportBug;
		helpMe = new PMenuItem(ifa.translate("help")); about = new PMenuItem(ifa.translate("about")); 
		reportBug = new PMenuItem(ifa.translate("report a bug"));

		help.add(helpMe); help.add(about); help.add(reportBug);

		return menu;
	}
	private PPanel getBuddyList(){
		GridBagConstraints c = new GridBagConstraints();
		PPanel buddyList = new PPanel();	
		buddyList.setLayout(new GridBagLayout());
		c.gridx = 0;
		c.fill = GridBagConstraints.BOTH;
		c.gridy = 0;
		POpaqueLabel chumroll = new POpaqueLabel(ifa.translate("chumroll").toUpperCase()+":");
		buddyList.add(chumroll, c);

		c.weighty = 1;
		c.gridy = 1;
		c.gridwidth = 3;
		friends = new FriendPane(this, ifa);
		this.redrawFriends();
		buddyList.add(friends, c);

		///////////////////
		c.gridwidth = 1;
		c.weighty = 0;
		c.gridy = 2;
		c.weightx = 0.5;
		addchum = new PButton(ifa.translate("add chum")); 
		addchum.addActionListener(this);
		buddyList.add(addchum, c);
		////////////////////
		c.gridx = 1;
		PButton block = new PButton(ifa.translate("block")); 
		buddyList.add(block, c);
		/////////////////////
		c.gridx = 2;
		pester = new PButton(ifa.translate("pester")+"!"); 
		pester.addActionListener(this);
		buddyList.add(pester, c);
		return buddyList;
	}
	private PPanel getMoods(){
		PPanel moods = new PPanel();
		GridBagLayout gridBagLayout = new GridBagLayout();
		moods.setLayout(gridBagLayout);
		GridBagConstraints c = new GridBagConstraints();
		PButton chummy = new PButton(ifa.translate("chummy"));
		c.weightx = 0.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		moods.add(chummy,c);

		PButton bully = new PButton(ifa.translate("bully"));
		c.weightx = 0.7;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;

		moods.add(bully, c);

		PButton abscond = new PButton(ifa.translate("abscond"));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 1;
		moods.add(abscond, c);
		return moods;
	}
	public Messaging getChat(String user){
		for(Messaging m: messWindows){
			if(m.getUser().equals(user)){
				m.toFront();
				return m;
			}
		}
		Messaging m = new Messaging(ifa, user, this);
		messWindows.add(m);
		return m;
	}
	public void closeChat(Messaging m){
		if(messWindows.contains(m)){
			messWindows.remove(m);
		}
	}
	public void setColor(Color c){
		ifa.setColor(c);
	}
	public void message(String user){
		getChat(user);
	}
	private void redrawFriends(){
		friends.redraw();
		this.validate();
		this.repaint();
	}
	public void incomingMessage(Message message){
		getChat(message.getFrom()).incoming(message);
	}
	public void loginResponse(boolean success) {
		login.loginResponse(success);
		if(success){
			login.setVisible(false);
			this.setVisible(true);
		}
		this.redrawFriends();
	}
	public boolean reconnect(){
		String u = login.getUsername();
		String p = login.getPassword();
		boolean suc = false;
		try {
			suc = ifa.connect(ifa.getSettings().getString("host"), ifa.getSettings().getInt("port"));
		} catch (SettingsException e) {
			Log.getInstance().error("Could not reconnect to server");
			return false;
		}
		if(suc){
			ifa.login(u, p);
		}
		return true;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand()){
		case "X":
			System.exit(0);
			break;
		case "_":
			this.setState(Frame.ICONIFIED);
			break;
		case "reconnect":
			ifa.close();
			this.setVisible(false);
			if(!reconnect()){
				//if the connection could not be made, present an option to try again
				this.timeout();
			}
			break;
		case "color":
			ColorChooser cc = new ColorChooser(this);
			cc.setLocationRelativeTo(this);
			break;
		}
		if(e.getSource()==addchum){
			String chum = JOptionPane.showInputDialog(ifa.translate("Enter chums name"));
			if(chum!=null){
				if(!Util.verifyUsername(chum)){
					JOptionPane.showMessageDialog(this,
							Util.usernameFailureReason(chum),
							ifa.translate("error"),
							JOptionPane.ERROR_MESSAGE);
				}else{
					ifa.addFriend(chum);
				}
			}
		}else if(e.getSource()==pester&&friends.getSelectedUser()!=null){
			this.getChat(friends.getSelectedUser());
		}
	}
	public void friendRequestResponse(String username, boolean found){
		if(found){
			redrawFriends();
		}else{
			JOptionPane.showMessageDialog(this,
					ifa.translate("user not found"),
					"Pesterchum",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	@Override
	public void timeout() {
		this.setVisible(false);
		Object[] options = {ifa.translate("Reconnect"),
		ifa.translate("Quit")};
		int n = JOptionPane.showOptionDialog(this,
				ifa.translate("Would you like to reconnect?"),
				ifa.translate("Connection Lost"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				new ImageIcon(ResourceLoader.getIcon()),
				options,
				options[0]);
		if(n==0){
			if(!reconnect()){
				//if the connection could not be made, present the options again
				this.timeout();
			}
		}else{
			System.exit(0);
		}
	}
	@Override
	public void updateRequired(int serverVersion) {
		Log.getInstance().info("Client outdated, update necessary");
		//TODO link to gui
	}			
}
