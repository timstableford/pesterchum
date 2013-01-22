package pesterchum.client.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import pesterchum.client.connection.Interface;
import pesterchum.client.data.Message;
import pesterchum.client.gui.login.Login;
import pesterchum.client.gui.theme.PButton;
import pesterchum.client.gui.theme.PFrame;
import pesterchum.client.gui.theme.PMenu;
import pesterchum.client.gui.theme.PMenuBar;
import pesterchum.client.gui.theme.PMenuItem;
import pesterchum.client.gui.theme.PPanel;
import pesterchum.client.resource.Img;
import pesterchum.client.resource.Resource;
import pesterchum.client.resource.ResourceLoader;

public class GUI extends PFrame implements ActionListener{
	private static final long serialVersionUID = 1L;
	private ResourceLoader smilies;
	private Interface ifa; //this has the methods you will communicate with
	public GUI(){
		ifa = new Interface(this);
		this.setTitle("Pesterchum");
		this.setUndecorated(true);
		Login l = new Login("localhost",7423,ifa);
		PPanel logoPlace, moods, buddyList;
		PButton chummy, palsy, chipper, bully, preppy, rancorous, abscond,
		addChum, block, pester;
		//menu objects
		PMenuBar menu;
		PMenu client, profile, help;
		PMenuItem options, memos, pesterLog, randomEcounter, userList, idle, addGroup,
		importThings, reconnect, exit, helpMe, about, reportBug;
		
		// menu at top
		// logo
		//list of chums
		//buttons add chum/block[red]/pester!
		//mychumhandle: mood/ nick/color
		//mood table 2x6 + abscond
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		smilies = new ResourceLoader();
		smilies.load("/smilies/smilies.xml");
		this.setSize(new Dimension(200, 400));
		
		//create menu bar + menus
		menu= new PMenuBar();
		menu.add(Box.createHorizontalStrut(5));
		client = new PMenu("client"); profile = new PMenu("profile"); help = new PMenu("help"); 
		
		PMenuItem quit = new PMenuItem("X");
		PMenuItem min = new PMenuItem("_");
		quit.addActionListener(this);
		min.addActionListener(this);
		menu.add(client); menu.add(profile); menu.add(help);
		menu.add(Box.createHorizontalGlue());
		menu.add(min);
		menu.add(quit);
		//set up the Client menu
		options = new PMenuItem("options"); memos = new PMenuItem("memos");
		pesterLog = new PMenuItem("pesterlog"); randomEcounter = new PMenuItem("random ecounter"); 
		userList = new PMenuItem("user list"); idle = new PMenuItem("idle"); 
		addGroup = new PMenuItem("add group"); importThings = new PMenuItem("import"); 
		reconnect = new PMenuItem("reconnect"); exit = new PMenuItem("exit");
		
		client.add(options); client.add(memos); client.add(pesterLog); client.add(randomEcounter);
		client.add(userList); client.add(idle); client.add(addGroup); client.add(importThings);
		client.add(reconnect); client.add(exit);
		
		//set up the Help menu
		helpMe = new PMenuItem("help"); about = new PMenuItem("about"); reportBug = new PMenuItem("report a bug");
		
		help.add(helpMe); help.add(about); help.add(reportBug);
		
		//add the whole thing
		this.add(menu, BorderLayout.NORTH);
		
		//try for chumroll
		buddyList = new PPanel();	
		JTextArea chumroll = new JTextArea("set server connection to get and\ndisplay names 1 per line", 5, 15);
		chumroll.setEditable(false);
		//set proper layout for the buttons under list
		addChum = new PButton("add chum"); block = new PButton("block"); pester = new PButton("pester!"); 
		buddyList.add(chumroll); buddyList.add(addChum); buddyList.add(block); buddyList.add(pester);
		this.add(buddyList);
		
		//try for grid layout for moods - SWITCH to GridBag Layout
		moods = new PPanel();
		
		//buttons create + add
		chummy = new PButton("chummy"); palsy = new PButton("palsy"); chipper = new PButton("chipper"); bully = new PButton("bully");
		preppy = new PButton("peppy"); rancorous= new PButton("rancorous"); abscond = new PButton("abscond"); 
		
		moods.add(chummy); moods.add(bully); moods.add(palsy); moods.add(preppy); moods.add(chipper); moods.add(rancorous);moods.add(abscond);
		//set grid
		GridLayout gridLayout = new GridLayout(0,2);
		moods.setLayout(gridLayout);
		
		this.add(moods, BorderLayout.SOUTH);
		
		//logo time!
		//this.add(new JLabel(getIcon(smilies.getResource("apple"))), BorderLayout.CENTER);
		//this.add(new PLabel("Label", getIcon(smilies.getResource("apple"))),BorderLayout.CENTER);
		
		this.setVisible(true);
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

}
