package pesterchum.client.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

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

public class GUI extends PFrame implements ActionListener{
	private static final long serialVersionUID = 1L;
	private ResourceLoader smilies, theme;
	private Login login;
	private Interface ifa; //this has the methods you will communicate with
	public GUI(){
		ifa = new Interface(this);
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
		PMenu client = new PMenu("client");
		PMenu profile = new PMenu("profile"); 
		PMenu help = new PMenu("help"); 
		
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
		PMenuItem importThings, reconnect, exit;
		options = new PMenuItem("options"); memos = new PMenuItem("memos");
		pesterLog = new PMenuItem("pesterlog"); randomEncounter = new PMenuItem("random ecounter"); 
		userList = new PMenuItem("user list"); idle = new PMenuItem("idle"); 
		addGroup = new PMenuItem("add group"); importThings = new PMenuItem("import"); 
		reconnect = new PMenuItem("reconnect"); exit = new PMenuItem("exit");
		
		client.add(options); client.add(memos); client.add(pesterLog); client.add(randomEncounter);
		client.add(userList); client.add(idle); client.add(addGroup); client.add(importThings);
		client.add(reconnect); client.add(exit);
		
		//set up the Profile menu
		PMenuItem quirks, trollSlum, color, switchChum;
		quirks = new PMenuItem("quirks"); trollSlum = new PMenuItem("trollslum"); color = new PMenuItem("color"); 
		switchChum = new PMenuItem("switch");
		profile.add(quirks); profile.add(trollSlum); profile.add(color); profile.add(switchChum);
		
		//set up the Help menu
		PMenuItem helpMe, about, reportBug;
		helpMe = new PMenuItem("help"); about = new PMenuItem("about"); reportBug = new PMenuItem("report a bug");
		
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
		POpaqueLabel chumroll = new POpaqueLabel("CHUMROLL:");
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
		PButton addChum = new PButton("add chum"); 
		buddyList.add(addChum, c);
		////////////////////
		c.gridx = 1;
		PButton block = new PButton("block"); 
		buddyList.add(block, c);
		/////////////////////
		c.gridx = 2;
		PButton pester = new PButton("pester!"); 
		buddyList.add(pester, c);
		return buddyList;
	}
	private PPanel getMoods(){
		PPanel moods = new PPanel();
		GridBagLayout gridBagLayout = new GridBagLayout();
		moods.setLayout(gridBagLayout);
		GridBagConstraints c = new GridBagConstraints();
		PButton chummy = new PButton("chummy");
		c.weightx = 0.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		moods.add(chummy,c);
		
		PButton bully = new PButton("bully");
		c.weightx = 0.7;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		
		moods.add(bully, c);
		
		PButton abscond = new PButton("abscond");
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
		System.out.println("Message from "+message.getFrom()+" - "+message.getMessage());
	}
	
	public void loginResponse(boolean success) {
		login.loginResponse(success);
		if(success){
			login.setVisible(false);
			this.setVisible(true);
		}
		System.out.println("Login response - successful? "+success);
	}
	
	public void versionMismatch(int client, int server){
		
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

