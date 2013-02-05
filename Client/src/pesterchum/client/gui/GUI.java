package pesterchum.client.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import pesterchum.client.PesterchumGUI;
import pesterchum.client.connection.Interface;
import pesterchum.client.connection.SettingsException;
import pesterchum.client.data.Message;
import pesterchum.client.gui.theme.PButton;
import pesterchum.client.gui.theme.PFrame;
import pesterchum.client.gui.theme.PLabel;
import pesterchum.client.gui.theme.PMenu;
import pesterchum.client.gui.theme.PMenuBar;
import pesterchum.client.gui.theme.PMenuItem;
import pesterchum.client.gui.theme.POpaqueLabel;
import pesterchum.client.gui.theme.PPanel;
import pesterchum.client.resource.Img;
import pesterchum.client.resource.Resource;
import pesterchum.client.resource.ResourceLoader;

public class GUI extends PFrame implements ActionListener, PesterchumGUI{
	private static final long serialVersionUID = 1L;
	private ResourceLoader smilies, theme;
	private Login login;
	private Interface ifa; //this has the methods you will communicate with
	public GUI(){
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
			System.err.println("Could not load settings");
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
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.weighty = 1;
		c.anchor = GridBagConstraints.PAGE_START;
		content.setLayout(new GridBagLayout());
		this.add(getMenu(), BorderLayout.NORTH);
		this.add(content, BorderLayout.CENTER);
		//logo
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(0,6,0,6);
		content.add(new JLabel(new ImageIcon(theme.getImage("logo"))), c);
		//////
		c.gridy = 1;
		content.add(getBuddyList(),c);
		//////
		c.anchor = GridBagConstraints.PAGE_END;
		c.gridy = 1;
		c.insets = new Insets(0,6,6,6);
		content.add(getMoods(),c);
	}
	private PMenuBar getMenu(){
		PMenuBar menu = new PMenuBar();
		menu.add(Box.createHorizontalStrut(5));
		PMenu client = new PMenu(ifa.translate("client"));
		PMenu profile = new PMenu(ifa.translate("profile")); 
		PMenu help = new PMenu(ifa.translate("help")); 
		
		PMenuItem quit = new PMenuItem("X");
		PMenuItem min = new PMenuItem("_");
		quit.addActionListener(this);
		min.addActionListener(this);
		menu.add(client); menu.add(profile); menu.add(help);
		menu.add(Box.createHorizontalGlue());
		menu.add(min);
		menu.add(quit);
		//set up the Client menu
		PMenuItem options, memos, pesterLog, randomEncounter, userList, idle, addGroup;
		PMenuItem reconnect, exit;
		options = new PMenuItem(ifa.translate("options")); memos = new PMenuItem(ifa.translate("memos"));
		pesterLog = new PMenuItem(ifa.translate("pesterlog")); randomEncounter = new PMenuItem(ifa.translate("random encounter")); 
		userList = new PMenuItem(ifa.translate("user list")); idle = new PMenuItem(ifa.translate("idle")); 
		addGroup = new PMenuItem(ifa.translate("add group")); 
		reconnect = new PMenuItem(ifa.translate("reconnect")); exit = new PMenuItem(ifa.translate("exit"));
		
		client.add(options); client.add(memos); client.add(pesterLog); client.add(randomEncounter);
		client.add(userList); client.add(idle); client.add(addGroup);
		client.add(reconnect); client.add(exit);
		
		//set up the Profile menu
		PMenuItem quirks, trollSlum, color, switchChum;
		quirks = new PMenuItem(ifa.translate("quirks")); trollSlum = new PMenuItem(ifa.translate("trollslum")); 
		color = new PMenuItem(ifa.translate("color")); 
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
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 0;
		POpaqueLabel chumroll = new POpaqueLabel(ifa.translate("chumroll").toUpperCase()+":");
		buddyList.add(chumroll, c);
		
		
		c.gridy = 1;
		PPanel friendPanel = new PPanel();
		friendPanel.setLayout(new BoxLayout(friendPanel, BoxLayout.Y_AXIS));
		Box box = Box.createVerticalBox();
		box.add(new PLabel("a"));
		box.add(new PLabel("b"));
		box.add(new PLabel("c"));
		
		JScrollPane jscrlpBox = new JScrollPane(box);
		friendPanel.add(jscrlpBox);
		
		c.gridwidth = 3;
		buddyList.add(friendPanel, c);
		///////////////////
		c.gridwidth = 1;
		c.gridy = 2;
		c.weightx = 0.5;
		PButton addChum = new PButton(ifa.translate("add chum")); 
		buddyList.add(addChum, c);
		////////////////////
		c.gridx = 1;
		PButton block = new PButton(ifa.translate("block")); 
		buddyList.add(block, c);
		/////////////////////
		c.gridx = 2;
		PButton pester = new PButton(ifa.translate("pester")+"!"); 
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
	private ImageIcon getIcon(Resource res){
		if(res instanceof Img){
			Img img = (Img)res;
			ImageIcon i = new ImageIcon(img.getImage());
			return i;
		}
		return null;
	}
	public void incomingMessage(Message message){
		System.out.println("Message from "+message.getFrom()+" - "+message.getContent());
	}
	
	public void loginResponse(boolean success) {
		login.loginResponse(success);
		if(success){
			login.setVisible(false);
			this.setVisible(true);
		}
		System.out.println("Login response - successful? "+success);
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
		}
	}


	public void friendRequestResponse(String username, boolean found){

	}
}			

